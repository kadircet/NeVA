//
//  NevaTabBarController.swift
//  NeVA
//
//  Created by Bilal Yaylak on 2.01.2018.
//  Copyright © 2018 Bilemiyorum Altan. All rights reserved.
//

import UIKit
import CoreData
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
        //print(UserToken.email!)
        //print(UserToken.token!.base64EncodedString())
        let predicate = NSPredicate(format: "(userMail == %@)", argumentArray: [UserToken.email!])
        fetchRequest.predicate = predicate
        var lastEntryNumber: UInt32 = 0
        do {
            let fetchedEntries = try managedObjectContext.fetch(fetchRequest) as! [HistoryEntry]
            if !fetchedEntries.isEmpty {
                lastEntryNumber = UInt32(fetchedEntries[0].choice_id)
            }
        } catch (let error) {
            fatalError("Failed to fetch: \(error)")
        }
        //
        print("Last entry number \(lastEntryNumber)")
        
        //Fetch history entries from server and commit to the coredata
        let service = NevaConstants.service
        var request = Neva_Backend_FetchUserHistoryRequest()
        request.startIndex = lastEntryNumber
        request.token = UserToken.token!
        do {
            let response = try service.fetchuserhistory(request)
            let history = response.userHistory
            print("\(history.history.count) entries were fetched from server")
            //print(history.userID)
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
                        print("Meal is not valid")
                    } else {
                        historyEntry.meal = fetchedMeals[0]
                    }
                    try managedObjectContext.save()
                } catch (let error) {
                    fatalError("Failed to fetch: \(error)")
                }
            }
        } catch (let error) {
            print(error)
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
            print(responseMessage)
            tagsAcquired = responseMessage.tagList
        } catch (let error) {
            print(error)
        }
        
        //Update or create tags
        for tagAcquired in tagsAcquired {
            let fetchRequest = NSFetchRequest<NSFetchRequestResult>(entityName: "Tag")
            fetchRequest.predicate = NSPredicate(format: "id==%@", argumentArray: [tagAcquired.id])
            do {
                let fetchedEntries = try managedObjectContext.fetch(fetchRequest) as? [Tag]
                if let tags = fetchedEntries, !tags.isEmpty {
                    print("Updating tag \(tagAcquired.id)")
                    let tag = tags[0]
                    tag.name = tagAcquired.name
                } else {
                    print("Creating tag \(tagAcquired.id)")
                    let tag = NSEntityDescription.insertNewObject(forEntityName: "Tag", into: managedObjectContext) as! Tag
                    tag.id = Int32(tagAcquired.id)
                    tag.name = tagAcquired.name
                }
            } catch (let error){
                fatalError("Failed to fetch: \(error)")
            }
        }
        //Commit changes
        do {
            try managedObjectContext.save()
        } catch (let error){
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
            print(responseMessage)
            mealsAcquired = responseMessage.items.suggestionList
            defaults.set(Int(responseMessage.lastUpdated), forKey: "lastUpdatedMealID")
        } catch (let error) {
            print(error)
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
                    print("Updating meal \(mealAcquired.suggesteeID)")
                    meal = meals[0]
                    meal!.name = mealAcquired.name
                } else {
                    print("Creating meal \(mealAcquired.suggesteeID)")
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
                    fatalError("Failed to fetch: \(error)")
                }
                //Save Tags of meal
                for tagOfMeal in mealAcquired.tags {
                    let tag = tagsOfDatabase[Int32(tagOfMeal.id)]
                    print("\(tag!.id):\(tag!.name!) is added to \(meal!.id):\(meal!.name!)")
                    meal?.addToTags(tag!)
                }
            } catch (let error){
                fatalError("Failed to fetch: \(error)")
            }
        }
        //
        
        //Commit changes
        do {
            try managedObjectContext.save()
        } catch (let error){
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
