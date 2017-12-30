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
        recommendationView.isHidden = true
        getMealsFromServer()
        // Do any additional setup after loading the view.
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    func getMealsFromServer() {
        
        guard let appDelegate =
            UIApplication.shared.delegate as? AppDelegate else {
                return
        }
        
        let managedObjectContext =
            appDelegate.databaseContext
        
        var startIndex = 0
        //TODO: Change fetching and updating system, just a temporary solution
        //Finding maximum id in database
        /*let fetchRequest = NSFetchRequest<NSFetchRequestResult>(entityName: "Meal")
        fetchRequest.predicate = NSPredicate(format: "id==max(id)", argumentArray: nil)
        do {
            let fetchedEntries = try managedObjectContext.fetch(fetchRequest) as? [Meal]
            if let maximumMeal = fetchedEntries, !maximumMeal.isEmpty {
                startIndex = Int(maximumMeal[0].id)
            }
        } catch {
            fatalError("Failed to fetch: \(error)")
        }*/
        //
        
        //Get meals from server
        let service = NevaConstants.service
        var request = Neva_Backend_GetSuggestionItemListRequest()
        request.startIndex = UInt32(startIndex)
        request.token = UserToken.token!
        request.suggestionCategory = .meal
        var itemsAcquired: [Neva_Backend_Suggestion] = []
        do {
            let responseMessage = try service.getsuggestionitemlist(request)
            print(responseMessage)
            itemsAcquired = responseMessage.items.suggestionList
        } catch (let error) {
            print(error)
        }
        //
        
        //Update or add items
        for item in itemsAcquired {
            let fetchRequest = NSFetchRequest<NSFetchRequestResult>(entityName: "Meal")
            fetchRequest.predicate = NSPredicate(format: "id==%@", argumentArray: [item.suggesteeID])
            do {
                let fetchedEntries = try managedObjectContext.fetch(fetchRequest) as? [Meal]
                //TODO: update tags and pictures too
                if let meals = fetchedEntries, !meals.isEmpty {
                    print("Updating meal \(item.suggesteeID)")
                    let meal = meals[0]
                    meal.name = item.name
                } else {
                    print("Creating meal \(item.suggesteeID)")
                    let meal = NSEntityDescription.insertNewObject(forEntityName: "Meal", into: managedObjectContext) as! Meal
                    meal.id = Int32(item.suggesteeID)
                    meal.name = item.name
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
