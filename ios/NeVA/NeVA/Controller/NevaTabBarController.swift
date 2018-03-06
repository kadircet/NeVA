//
//  NevaTabBarController.swift
//  NeVA
//
//  Created by Bilal Yaylak on 2.01.2018.
//  Copyright © 2018 Bilemiyorum Altan. All rights reserved.
//

import UIKit
import CoreData
import TTGSnackbar
import os

class NevaTabBarController: UITabBarController {

    override func viewDidLoad() {
        super.viewDidLoad()
        if #available(iOS 10.0, *) {
            //self.tabBar.tintColor = NeVAUtils.hexStringToUIColor(hex: "#BF360C")
            self.tabBar.unselectedItemTintColor = NeVAUtils.hexStringToUIColor(hex: "#FFCCBC")
        }
        getTagsFromServer()
        getMealsFromServer()
        fetchEntriesFromServer()
        // Do any additional setup after loading the view.
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    func fetchEntriesFromServer() {
        guard let appDelegate =
            UIApplication.shared.delegate as? AppDelegate else {
                return
        }
        let managedObjectContext =
            appDelegate.databaseContext
        
        //Find the last choice id for the user
        let fetchRequest = NSFetchRequest<NSFetchRequestResult>(entityName: "HistoryEntry")
        let sortDescriptor = NSSortDescriptor(key: "choice_id", ascending: false)
        fetchRequest.sortDescriptors = [sortDescriptor]
        fetchRequest.fetchLimit = 1
        let predicate = NSPredicate(format: "(userMail == %@)", argumentArray: [UserToken.email!])
        fetchRequest.predicate = predicate
        var lastEntryNumber: UInt32 = 0
        do {
            let fetchedEntries = try managedObjectContext.fetch(fetchRequest) as! [HistoryEntry]
            if !fetchedEntries.isEmpty {
                lastEntryNumber = UInt32(fetchedEntries[0].choice_id)
            }
        } catch (let error) {
            if #available(iOS 10.0, *) {
                os_log("Error: %@", log: NevaConstants.logger, type: .fault, String(describing: error))
            }
            fatalError("Failed to fetch: \(error)")
        }
        //
        if #available(iOS 10.0, *) {
            os_log("Last entry number: %@", log: NevaConstants.logger, type: .info, String(describing: lastEntryNumber))
        } else {
            print("Last entry number \(lastEntryNumber)")
        }
        
        //Fetch history entries from server and commit to the coredata
        let service = NevaConstants.service
        var request = Neva_Backend_FetchUserHistoryRequest()
        request.startIndex = lastEntryNumber
        request.token = UserToken.token!
        do {
            let response = try service.fetchuserhistory(request)
            let history = response.userHistory
            if #available(iOS 10.0, *) {
                os_log("%@ entries were fetched from server", log: NevaConstants.logger, type: .info, history.history.count)
            } else {
                // Fallback on earlier versions
                print("\(history.history.count) entries were fetched from server")
            }
            for choice in history.history {
                let historyEntry = NSEntityDescription.insertNewObject(forEntityName: "HistoryEntry", into: managedObjectContext) as! HistoryEntry
                historyEntry.choice_id = Int64(choice.choiceID)
                let seconds = Double(choice.timestamp.seconds) + (Double(choice.timestamp.nanos)*1e-9)
                let date = Date(timeIntervalSince1970: seconds)
                historyEntry.date = date
                historyEntry.userMail = UserToken.email!
                let fetchRequest = NSFetchRequest<NSFetchRequestResult>(entityName: "Meal")
                let predicate = NSPredicate(format: "(id == %@)", argumentArray: [Int32(choice.suggesteeID)])
                fetchRequest.predicate = predicate
                do {
                    let fetchedMeals = try managedObjectContext.fetch(fetchRequest) as! [Meal]
                    if fetchedMeals.isEmpty {
                        if #available(iOS 10.0, *) {
                            os_log("Meal is not valid", log: NevaConstants.logger, type: .error)
                        } else {
                            // Fallback on earlier versions
                            print("Meal is not valid")
                        }
                    } else {
                        historyEntry.meal = fetchedMeals[0]
                    }
                    try managedObjectContext.save()
                } catch (let error) {
                    if #available(iOS 10.0, *) {
                        os_log("Error: %@", log: NevaConstants.logger, type: .fault, String(describing: error))
                    }
                    fatalError("Failed to fetch: \(error)")
                }
            }
        } catch (let error) {
            if #available(iOS 10.0, *) {
                os_log("Error: %@", log: NevaConstants.logger, type: .error, String(describing: error))
            } else {
                // Fallback on earlier versions
                print("Error: \(error)")
            }
            if let clientError = error as? Neva_Backend_BackendClientError, case let .error(e) = clientError {
                let snackbar = TTGSnackbar(message: e.statusMessage ?? "UNDEFINED ERROR", duration: .middle)
                snackbar.backgroundColor = NeVAColors.primaryDarkColor
                snackbar.shouldDismissOnSwipe = true
                snackbar.show()
            }
        }
    }
    func getTagsFromServer() {
        guard let appDelegate =
            UIApplication.shared.delegate as? AppDelegate else {
                return
        }
        let managedObjectContext = appDelegate.databaseContext
        var startIndex = 0
        // Find start index
        let fetchRequest = NSFetchRequest<NSFetchRequestResult>(entityName: "Tag")
        fetchRequest.predicate = NSPredicate(format: "id==max(id)", argumentArray: nil)
        do {
            let fetchedEntries = try managedObjectContext.fetch(fetchRequest) as? [Tag]
            if let maximumTagID = fetchedEntries, !maximumTagID.isEmpty {
                startIndex = Int(maximumTagID[0].id)
            }
        } catch {
            if #available(iOS 10.0, *) {
                os_log("Error: %@", log: NevaConstants.logger, type: .fault, String(describing: error))
            }
            fatalError("Failed to fetch: \(error)")
        }
        // Fetch tags from server
        let service = NevaConstants.service
        var request = Neva_Backend_GetTagsRequest()
        request.startIndex = UInt32(startIndex)
        request.token = UserToken.token!
        var tagsAcquired: [Neva_Backend_Tag] = []
        do {
            let responseMessage = try service.gettags(request)
            tagsAcquired = responseMessage.tagList
        } catch (let error) {
            if #available(iOS 10.0, *) {
                os_log("Error: %@", log: NevaConstants.logger, type: .error, String(describing: error))
            } else {
                // Fallback on earlier versions
                print("Error: \(error)")
            }
            if let clientError = error as? Neva_Backend_BackendClientError, case let .error(e) = clientError {
                let snackbar = TTGSnackbar(message: e.statusMessage ?? "UNDEFINED ERROR", duration: .middle)
                snackbar.backgroundColor = NeVAColors.primaryDarkColor
                snackbar.shouldDismissOnSwipe = true
                snackbar.show()
            }
        }
        
        //Update or create tags
        for tagAcquired in tagsAcquired {
            let fetchRequest = NSFetchRequest<NSFetchRequestResult>(entityName: "Tag")
            fetchRequest.predicate = NSPredicate(format: "id==%@", argumentArray: [tagAcquired.id])
            do {
                let fetchedEntries = try managedObjectContext.fetch(fetchRequest) as? [Tag]
                if let tags = fetchedEntries, !tags.isEmpty {
                    print("Updating tag \(tagAcquired.id)")
                    if #available(iOS 10.0, *) {
                        os_log("Updating tag %@", log: NevaConstants.logger, type: .info, tagAcquired.id)
                    } else {
                        // Fallback on earlier versions
                        print("Updating tag \(tagAcquired.id)")
                    }
                    let tag = tags[0]
                    tag.name = tagAcquired.name
                } else {
                    if #available(iOS 10.0, *) {
                        os_log("Creating tag %@", log: NevaConstants.logger, type: .info, tagAcquired.id)
                    } else {
                        // Fallback on earlier versions
                        print("Creating tag \(tagAcquired.id)")
                    }
                    let tag = NSEntityDescription.insertNewObject(forEntityName: "Tag", into: managedObjectContext) as! Tag
                    tag.id = Int32(tagAcquired.id)
                    tag.name = tagAcquired.name
                }
            } catch (let error){
                if #available(iOS 10.0, *) {
                    os_log("Error: %@", log: NevaConstants.logger, type: .fault, String(describing: error))
                }
                fatalError("Failed to fetch: \(error)")
            }
        }
        //Commit changes
        do {
            try managedObjectContext.save()
        } catch (let error){
            if #available(iOS 10.0, *) {
                os_log("Error: %@", log: NevaConstants.logger, type: .fault, String(describing: error))
            }
            fatalError("Failed to fetch: \(error)")
        }
    }
    func getMealsFromServer() {
        guard let appDelegate =
            UIApplication.shared.delegate as? AppDelegate else {
                return
        }
        let managedObjectContext = appDelegate.databaseContext
        let defaults = UserDefaults.standard
        let startIndex = defaults.integer(forKey: "lastUpdatedMealID")
        
        //Get meals from server
        let service = NevaConstants.service
        var request = Neva_Backend_GetSuggestionItemListRequest()
        request.startIndex = UInt32(startIndex)
        request.token = UserToken.token!
        request.suggestionCategory = .meal
        var mealsAcquired: [Neva_Backend_Suggestion] = []
        do {
            let responseMessage = try service.getsuggestionitemlist(request)
            mealsAcquired = responseMessage.items.suggestionList
            if #available(iOS 10.0, *) {
                os_log("Meal list is received %@ ", log: NevaConstants.logger, type: .info, String(describing: responseMessage.items.suggestionList.map({($0.suggesteeID,$0.name)})))
            } else {
                print("Meal list is received \(responseMessage.items.suggestionList.map({($0.suggesteeID,$0.name)}))")
            }
            defaults.set(Int(responseMessage.lastUpdated), forKey: "lastUpdatedMealID")
        } catch (let error) {
            if #available(iOS 10.0, *) {
                os_log("Error: %@", log: NevaConstants.logger, type: .error, String(describing: error))
            } else {
                // Fallback on earlier versions
                print("Error: \(error)")
            }
            if let clientError = error as? Neva_Backend_BackendClientError, case let .error(e) = clientError {
                let snackbar = TTGSnackbar(message: e.statusMessage ?? "UNDEFINED ERROR", duration: .middle)
                snackbar.backgroundColor = NeVAColors.primaryDarkColor
                snackbar.shouldDismissOnSwipe = true
                snackbar.show()
            }
        }
        //
        
        //Update or add items
        for mealAcquired in mealsAcquired {
            let fetchRequest = NSFetchRequest<NSFetchRequestResult>(entityName: "Meal")
            fetchRequest.predicate = NSPredicate(format: "id==%@", argumentArray: [mealAcquired.suggesteeID])
            do {
                let fetchedEntries = try managedObjectContext.fetch(fetchRequest) as? [Meal]
                //TODO: Update pictures too
                var meal: Meal? = nil
                if let meals = fetchedEntries, !meals.isEmpty {
                    if #available(iOS 10.0, *) {
                        os_log("Updating meal %@", log: NevaConstants.logger, type: .info, mealAcquired.suggesteeID)
                    } else {
                        // Fallback on earlier versions
                        print("Updating meal \(mealAcquired.suggesteeID)")
                    }
                    meal = meals[0]
                    meal!.name = mealAcquired.name
                } else {
                    if #available(iOS 10.0, *) {
                        os_log("Creating meal %@", log: NevaConstants.logger, type: .info, mealAcquired.suggesteeID)
                    } else {
                        // Fallback on earlier versions
                        print("Creating meal \(mealAcquired.suggesteeID)")
                    }
                    meal = (NSEntityDescription.insertNewObject(forEntityName: "Meal", into: managedObjectContext) as! Meal)
                    meal!.id = Int32(mealAcquired.suggesteeID)
                    meal!.name = mealAcquired.name
                }
                
                //Fetch all tags from coredata
                var tagsOfDatabase = [Int32: Tag]()
                let fetchRequest = NSFetchRequest<NSFetchRequestResult>(entityName: "Tag")
                do {
                    let fetchedTags = try managedObjectContext.fetch(fetchRequest) as! [Tag]
                    for fetchedTag in fetchedTags {
                        tagsOfDatabase[fetchedTag.id] = fetchedTag
                    }
                } catch (let error){
                    if #available(iOS 10.0, *) {
                        os_log("Error: %@", log: NevaConstants.logger, type: .fault, String(describing: error))
                    }
                    fatalError("Failed to fetch: \(error)")
                }
                //Save Tags of meal
                for tagOfMeal in mealAcquired.tags {
                    let tag = tagsOfDatabase[Int32(tagOfMeal.id)]
                    print("\(tag!.id):\(tag!.name!) is added to \(meal!.id):\(meal!.name!)")
                    meal?.addToTags(tag!)
                }
            } catch (let error){
                if #available(iOS 10.0, *) {
                    os_log("Error: %@", log: NevaConstants.logger, type: .fault, String(describing: error))
                }
                fatalError("Failed to fetch: \(error)")
            }
        }
        //
        
        //Commit changes
        do {
            try managedObjectContext.save()
        } catch (let error){
            if #available(iOS 10.0, *) {
                os_log("Error: %@", log: NevaConstants.logger, type: .fault, String(describing: error))
            }
            fatalError("Failed to fetch: \(error)")
        }
        //
        
    }

    /*
    // MARK: - Navigation

    // In a storyboard-based application, you will often want to do a little preparation before navigation
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        // Get the new view controller using segue.destinationViewController.
        // Pass the selected object to the new view controller.
    }
    */
    
}
