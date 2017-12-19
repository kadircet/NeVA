//
//  AddHistoryEntryViewController.swift
//  NeVA
//
//  Created by Bilal Yaylak on 4.12.2017.
//  Copyright © 2017 Bilemiyorum Altan. All rights reserved.
//

import UIKit
import CoreData

class AddHistoryEntryViewController: UIViewController {

    var date: Date?
    var meals: [Meal] = []
    var selectedMealIndex: Int?
    
    @IBOutlet weak var mealField: SearchTextField!
    @IBOutlet weak var dateLabel: UILabel!
    @IBOutlet weak var clockField: UITextField!
    private var timePickerOfClockField_: UIDatePicker = UIDatePicker()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view.
        
        if date == nil {
            date = Date().addingTimeInterval(TimeInterval(TimeZone.current.secondsFromGMT()))
        }
        var formatter = DateFormatter()
        formatter.timeZone = TimeZone(secondsFromGMT:0)
        formatter.dateFormat = "EEEE, MMM d, yyyy"
        dateLabel.text = formatter.string(from: date!)
        
        let inputView = UIView(frame: CGRect(x: 0, y: 0, width: self.view.frame.width, height: 240))
        timePickerOfClockField_.frame = CGRect(x: 0, y: 50, width: self.view.frame.width, height: 190)
        timePickerOfClockField_.date = date!
        timePickerOfClockField_.datePickerMode = .time
        timePickerOfClockField_.timeZone = TimeZone(secondsFromGMT: 0)
        timePickerOfClockField_.addTarget(self, action:
            #selector(AddHistoryEntryViewController.timePickerValueChanged(sender:)), for: UIControlEvents.valueChanged)
        inputView.addSubview(timePickerOfClockField_)
        let doneButton = UIButton(frame: CGRect(x: (self.view.frame.size.width/2) - (100/2), y: 0, width: 100, height: 50))
        doneButton.setTitle("Done", for: UIControlState.normal)
        doneButton.setTitle("Done", for: UIControlState.highlighted)
        doneButton.setTitleColor(UIColor.black, for: UIControlState.normal)
        doneButton.setTitleColor(UIColor.gray, for: UIControlState.highlighted)
        inputView.addSubview(doneButton)
        doneButton.addTarget(self, action: #selector(AddHistoryEntryViewController.doneButton(_:)), for: UIControlEvents.touchUpInside)
        clockField.inputView = inputView
        
        formatter = DateFormatter()
        formatter.timeZone = TimeZone(secondsFromGMT: 0)
        formatter.dateFormat = "HH:mm"
        clockField.text = formatter.string(from: timePickerOfClockField_.date)
        
        
        ///
        //let tap = UITapGestureRecognizer(target: self.view, action: #selector(UIView.endEditing(_:)))
        //tap.cancelsTouchesInView = false
        //self.view.addGestureRecognizer(tap)
        ///
        
        //Fetch meal names
        guard let appDelegate =
            UIApplication.shared.delegate as? AppDelegate else {
                return
        }
        let managedObjectContext =
            appDelegate.persistentContainer.viewContext
        let fetchRequest = NSFetchRequest<NSFetchRequestResult>(entityName: "Meal")
        do {
            let fetchedEntries = try managedObjectContext.fetch(fetchRequest) as? [Meal]
            if let meals = fetchedEntries, !meals.isEmpty {
                self.meals = meals
            }
        } catch (let error){
            fatalError("Failed to fetch: \(error)")
        }
        //
        
        //Search field init
        mealField.theme = .darkTheme()
        mealField.theme.fontColor = UIColor.white
        mealField.theme.bgColor = UIColor (red: 126.0/255.0, green: 136.0/255.0, blue: 141.0/255.0, alpha: 1)
        mealField.theme.borderColor = UIColor (red: 1, green: 1, blue: 1, alpha: 1)
        mealField.theme.separatorColor = UIColor (red: 1, green: 1, blue: 1, alpha: 0.5)
        mealField.comparisonOptions = [.caseInsensitive]
        mealField.filterStrings(meals.map{$0.name!})
        mealField.itemSelectionHandler = {filteredResults, itemPosition  in
            let item = filteredResults[itemPosition]
            self.mealField.text = item.title
            self.selectedMealIndex = itemPosition
        }
        //
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    @objc func timePickerValueChanged(sender: UIDatePicker) {
        let formatter = DateFormatter()
        formatter.timeZone = TimeZone(secondsFromGMT: 0)
        formatter.dateFormat = "HH:mm"
        clockField.text = formatter.string(from: sender.date)
    }
    
    @IBAction func backButtonPressed(_ sender: Any) {
        dismiss(animated: true, completion: nil)
    }
    @objc func doneButton(_ sender: Any) {
        self.view.endEditing(true)
    }
    
    @IBAction func addButtonPressed(_ sender: Any) {
        let mealName = mealField.text
        if mealName != nil {
            guard let appDelegate =
                UIApplication.shared.delegate as? AppDelegate else {
                    return
            }
            let managedObjectContext =
                appDelegate.persistentContainer.viewContext
            let fetchRequest = NSFetchRequest<NSFetchRequestResult>(entityName: "Meal")
            fetchRequest.predicate = NSPredicate(format: "name==%@", argumentArray: [mealName!])
            do {
                let fetchedEntries = try managedObjectContext.fetch(fetchRequest) as? [Meal]
                if let mealAte = fetchedEntries, !mealAte.isEmpty {
                    let meal = mealAte[0]
                    let service = NevaConstants.service
                    var request = Neva_Backend_InformUserChoiceRequest()
                    request.token = UserToken.token!
                    var choice = Neva_Backend_Choice()
                    //TODO GPS
                    choice.timestamp.seconds = UInt64(timePickerOfClockField_.date.timeIntervalSince1970)
                    choice.timestamp.nanos = 0
                    choice.suggesteeID = UInt32(meal.id)
                    request.choice = choice
                    do {
                        let response = try service.informuserchoice(request)
                        print(response)
                        let historyEntry = NSEntityDescription.insertNewObject(forEntityName: "HistoryEntry", into: managedObjectContext) as! HistoryEntry
                        historyEntry.choice_id = Int64(response.choiceID)
                        historyEntry.meal = meal
                        historyEntry.date = timePickerOfClockField_.date
                        historyEntry.userMail = UserToken.email!
                        do {
                            try managedObjectContext.save()
                            NotificationCenter.default.post(name: Notification.Name("reloadHistoryTable"), object: nil)
                            dismiss(animated: true, completion: nil)
                        } catch (let error){
                            fatalError("Failed to fetch: \(error)")
                        }
                    } catch (let error) {
                        print(error)
                        mealField.shake()
                        return
                    }
                } else {
                    mealField.shake()
                    return
                }
            } catch {
                fatalError("Failed to fetch: \(error)")
            }
        } else {
            mealField.shake()
            return
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

