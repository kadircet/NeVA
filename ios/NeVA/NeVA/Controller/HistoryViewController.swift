//
//  HistoryViewController.swift
//  NeVA
//
//  Created by Bilal Yaylak on 28.11.2017.
//  Copyright © 2017 Bilemiyorum Altan. All rights reserved.
//

import UIKit
import CoreData

class HistoryViewController: UIViewController, UITableViewDelegate, UITableViewDataSource {
    
    var historyEntries: [HistoryEntry] = []
    @IBAction func addHistoryEntry(_ sender: UIButton) {
        performSegue(withIdentifier: "addHistoryEntry", sender: self)
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        var calendar = Calendar.current
        calendar.timeZone = TimeZone(secondsFromGMT: 0)!
        let date = Date().addingTimeInterval(TimeInterval(TimeZone.current.secondsFromGMT()))
        let today = calendar.startOfDay(for: date)
        var components = calendar.dateComponents([.year, .month, .day, .hour, .minute],from: today)
        components.day! += 1
        let tomorrow = calendar.date(from: components)!
        if datePickerOfDateField_.date < tomorrow {
            return historyEntries.count + 1
        } else {
            return historyEntries.count
        }
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let index = indexPath.row
        if index < historyEntries.count
        {
            if let cell = tableView.dequeueReusableCell(withIdentifier: ("historyCell")) as? HistoryTableViewCell {
                let meal = historyEntries[index].meal!
                if let picture = meal.picture as? UIImage {
                    cell.setFoodPicture(picture: picture )
                }
                cell.setName(name: meal.name!)
                cell.setTime(time: historyEntries[indexPath.row].date!)
                return cell
            }
        }
        else if index == historyEntries.count {
            if let cell = tableView.dequeueReusableCell(withIdentifier: "addHistoryEntryCell") {
                return cell
            }
        }
        return UITableViewCell()
        
    }
    

    @IBOutlet weak var dateField: UITextField!
    @IBOutlet weak var historyTable: UITableView!
    @IBOutlet weak var segmentControl: UISegmentedControl!
    
    private var datePickerOfDateField_: UIDatePicker = UIDatePicker()
    
    @IBAction func orderTypeChanged(_ sender: Any) {
        sortHistoryEntries()
        historyTable.reloadData()
    }
    
    func sortHistoryEntries() {
        if segmentControl.selectedSegmentIndex == 0 {
            //Name ordered
            historyEntries.sort{
                he1, he2 in
                if he1.meal!.name! != he2.meal!.name! {
                    return he1.meal!.name! < he2.meal!.name!
                } else {
                    return he1.date! < he2.date!
                }
            }
        } else if segmentControl.selectedSegmentIndex == 1 {
            //Time ordered
            historyEntries.sort{
                he1, he2 in
                if he1.date! != he2.date! {
                    return he1.date! < he2.date!
                } else {
                    return he1.meal!.name! < he2.meal!.name!
                }
            }
        }
    }
    
    @objc func datePickerValueChanged(sender:UIDatePicker) {
        let dateFormatter = DateFormatter()
        dateFormatter.dateFormat = "EEEE, MMM d, yyyy"
        dateFormatter.timeZone = TimeZone(secondsFromGMT: 0)
        dateField.text = dateFormatter.string(from: datePickerOfDateField_.date)
        reloadEntries()
        sortHistoryEntries()
        historyTable.reloadData()
    }
    
    @objc func reload() {
        reloadEntries()
        sortHistoryEntries()
        historyTable.reloadData()
    }
    func reloadEntries() {
        let email = UserToken.email!
        var calendar = Calendar.current
        calendar.timeZone = TimeZone(secondsFromGMT: 0)!
        let date = datePickerOfDateField_.date
        let dateFrom = calendar.startOfDay(for: date)
        var components = calendar.dateComponents([.year, .month, .day, .hour, .minute], from: dateFrom)
        components.day! += 1
        let dateTo = calendar.date(from: components)!
        let predicate = NSPredicate(format: "(userMail == %@) AND (%@ <= date) AND (date < %@)",
                                    argumentArray: [email, dateFrom, dateTo])
        guard let appDelegate =
            UIApplication.shared.delegate as? AppDelegate else {
                return
        }
        let managedObjectContext =
            appDelegate.persistentContainer.viewContext
        let fetchRequest = NSFetchRequest<NSFetchRequestResult>(entityName: "HistoryEntry")
        fetchRequest.predicate = predicate
        do {
            let fetchedEntries = try managedObjectContext.fetch(fetchRequest) as! [HistoryEntry]
            historyEntries = fetchedEntries
        } catch (let error) {
            fatalError("Failed to fetch: \(error)")
        }
    }
    
    func fetchEntriesFromServer() {
        guard let appDelegate =
            UIApplication.shared.delegate as? AppDelegate else {
                return
        }
        let managedObjectContext =
            appDelegate.persistentContainer.viewContext
        
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
    override func viewDidLoad() {
        super.viewDidLoad()
        fetchEntriesFromServer()
        
        dateField.inputView = datePickerOfDateField_
        datePickerOfDateField_.date = Date().addingTimeInterval(TimeInterval(TimeZone.current.secondsFromGMT()))
        datePickerOfDateField_.datePickerMode = .date
        datePickerOfDateField_.timeZone = TimeZone(secondsFromGMT: 0)
        datePickerOfDateField_.addTarget(self, action:
            #selector(HistoryViewController.datePickerValueChanged(sender:)), for: UIControlEvents.valueChanged)
        let dateFormatter = DateFormatter()
        dateFormatter.dateFormat = "EEEE, MMM d, yyyy"
        dateFormatter.timeZone = TimeZone(secondsFromGMT: 0)
        dateField.text = dateFormatter.string(from: datePickerOfDateField_.date)
        
        NotificationCenter.default.addObserver(self, selector: #selector(reload), name: .reloadHistoryTable, object: nil)

        
        reloadEntries()
        sortHistoryEntries()
        
        historyTable.dataSource = self
        
        let tap = UITapGestureRecognizer(target: self.view, action: #selector(UIView.endEditing(_:)))
        tap.cancelsTouchesInView = false
        self.view.addGestureRecognizer(tap)
        // Do any additional setup after loading the view.
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        if segue.identifier == "addHistoryEntry" {
            if let target = segue.destination as? AddHistoryEntryViewController {
                var calendar = Calendar.current
                calendar.timeZone = TimeZone(secondsFromGMT: 0)!
                var dateSelected = calendar.dateComponents([.year, .month, .day, .hour, .minute],
                                                           from: datePickerOfDateField_.date)
                let dateNow = calendar.dateComponents([.year, .month, .day, .hour, .minute],
                                                      from: Date().addingTimeInterval(TimeInterval(TimeZone.current.secondsFromGMT())))
                dateSelected.hour = dateNow.hour
                dateSelected.minute = dateNow.minute
                target.date = calendar.date(from: dateSelected)
            }
        }
    }
    
}

extension Notification.Name {
    static let reloadHistoryTable = Notification.Name("reloadHistoryTable")
}
