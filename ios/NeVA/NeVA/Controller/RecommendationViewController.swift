//
//  RecommendationViewController.swift
//  NeVA
//
//  Created by Bilal Yaylak on 28.11.2017.
//  Copyright © 2017 Bilemiyorum Altan. All rights reserved.
//

import UIKit
import CoreData

class RecommendationViewController: UIViewController {
    
    @IBOutlet weak var recommendationView: UIView!
    
    @IBOutlet weak var recommendationImage: UIImage!
    
    @IBOutlet weak var recommendationName: UILabel!
    
    @IBAction func getRecommendation(_ sender: Any) {
        activityIndicator.startAnimating()
        //recommendationView.isHidden = false
        if let button = sender as? UIButton {
            button.isHidden = true
        }
        
        var request = Neva_Backend_GetSuggestionRequest()
        request.token = UserToken.token!
        request.suggestionCategory = .meal
        print(request)
        let service = NevaConstants.service
        do {
            /*_ = try service.getmealsuggestion(request, completion: { reply, result in
                    print(result)
                    self.activityIndicator.stopAnimating()
                    if let button = sender as? UIButton {
                        button.isHidden = false
                    }
                    if reply != nil {
                        self.recommendationView.isHidden = false
                        self.recommendationName.text = reply!.name
                    }
                } )*/
            //TODO: DO IT ASYNC
            let reply = try service.getsuggestion(request)
            print(reply)
            self.activityIndicator.stopAnimating()
            if let button = sender as? UIButton {
                button.isHidden = false
            }
            self.recommendationView.isHidden = false
            self.recommendationName.text = reply.name
        } catch (let error){
            print(error)
            self.recommendationView.isHidden = false
            self.recommendationName.text = error.localizedDescription
            self.activityIndicator.stopAnimating()
            if let button = sender as? UIButton {
                button.isHidden = false
            }
        }
    }
    @IBOutlet weak var activityIndicator: UIActivityIndicatorView!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        print("merhaba")
        recommendationView.isHidden = true
        getTagsFromServer()
        getMealsFromServer()
        // Do any additional setup after loading the view.
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
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
