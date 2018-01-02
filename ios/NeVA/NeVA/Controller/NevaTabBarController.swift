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

    /*
    // MARK: - Navigation

    // In a storyboard-based application, you will often want to do a little preparation before navigation
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        // Get the new view controller using segue.destinationViewController.
        // Pass the selected object to the new view controller.
    }
    */

}
