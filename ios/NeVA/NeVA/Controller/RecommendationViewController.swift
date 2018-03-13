//
//  RecommendationViewController.swift
//  NeVA
//
//  Created by Bilal Yaylak on 28.11.2017.
//  Copyright © 2017 Bilemiyorum Altan. All rights reserved.
//

import UIKit
import CoreData
import Koloda
import TTGSnackbar
import os

class RecommendationViewController: UIViewController, KolodaViewDelegate, KolodaViewDataSource {
    @IBOutlet weak var activityIndicator: UIActivityIndicatorView!
    
    var foods: [Meal] = []
    
    @IBAction func pressedDislike(_ sender: Any) {
        recommendationKolodaView.swipe(.left)
    }
    @IBAction func pressedLike(_ sender: Any) {
        recommendationKolodaView.swipe(.right)
    }
    //Koloda View Data Source and delegate functions
    func koloda(_ koloda: KolodaView, viewForCardAt index: Int) -> UIView {
        let view = RecommendationView()
        view.name.text = foods[index].name
        let color = UIColor.colorHash(name: foods[index].name)
        let componentColors = color.cgColor.components
        let colorBrightness = ((componentColors![0] * 299) + (componentColors![1] * 587) + (componentColors![2] * 114)) / 1000
        var textColor: UIColor? = nil
        if (colorBrightness < 0.5)
        {
            //Background is dark
            textColor = UIColor.white
        }
        else
        {
            //Background is white
            textColor = UIColor.black
        }
        let font = UIFont.systemFont(ofSize: 64.0)
        let initials = foods[index].name!.initials
        let index = initials.index(after: initials.startIndex)
        let firstLetter = String(initials[..<index])
        view.image.setImage(string: firstLetter, color: color, circular: true, textAttributes: [NSAttributedStringKey.font: font, NSAttributedStringKey.foregroundColor: textColor!])
        //view.image.image = UIImage(named: "loginRegisterViewBackground")
        return view
    }
    
    
    func kolodaNumberOfCards(_ koloda: KolodaView) -> Int {
        return foods.count
    }
    
    func kolodaSpeedThatCardShouldDrag(_ koloda: KolodaView) -> DragSpeed {
        return .fast
    }
    
    func kolodaDidRunOutOfCards(_ koloda: KolodaView) {
        getRecommendationList(koloda)
    }
    
    func koloda(_ koloda: KolodaView, didSwipeCardAt index: Int, in direction: SwipeResultDirection) {
        /* Enable for testing if mysqlpp is still not thread safe
         if index == foods.count-1 {
            return
        }*/
        var request = Neva_Backend_RecordFeedbackRequest()
        request.token = UserToken.token!
        var feedback = Neva_Backend_UserFeedback()
        switch direction {
            case .right:
                feedback.feedback = .like
            case .left:
                feedback.feedback = .dislike
            default:
                //Never
                feedback.feedback = .dislike
        }
        var choice = Neva_Backend_Choice()
        //TODO GPS
        choice.choiceID = getLastChoiceId()
        choice.timestamp.seconds = UInt64(Date().addingTimeInterval(TimeInterval(TimeZone.current.secondsFromGMT())).timeIntervalSince1970)
        choice.timestamp.nanos = 0
        choice.suggesteeID = UInt32(foods[index].id)
        feedback.choice = choice
        request.userFeedback = feedback
        let mealName = foods[index].name
        let service = NevaConstants.service
        do {
            let call = try service.recordfeedback(request) {responseMessage, callResult in
                if responseMessage != nil {
                    if #available(iOS 10.0, *) {
                        os_log("Feedback is registered.", log: NevaConstants.logger, type: .info)
                    } else {
                        print("Feedback is registered.")
                    }
                    if(direction == .right) {
                        DispatchQueue.main.async {
                            let snackbar = TTGSnackbar(message: "You liked " + String(describing: mealName ?? "unknown meal") + ", would you want to add it to your history?", duration: TTGSnackbarDuration.forever, actionText: "Yes", actionBlock: { snackbar in
                                self.quickAddLikedMealToHistory(mealName: mealName)
                                DispatchQueue.main.asyncAfter(deadline: DispatchTime.now() + Double(Int64(0.5 * Double(NSEC_PER_SEC))) / Double(NSEC_PER_SEC)) {
                                    snackbar.dismiss()
                                    let arrayOfTabBarItems = self.tabBarController!.tabBar.items
                                    if let barItems = arrayOfTabBarItems, barItems.count > 0 {
                                        for barItem in barItems {
                                            barItem.isEnabled = true
                                        }
                                    }
                                }
                            })
                            snackbar.backgroundColor = NeVAColors.primaryDarkColor
                            snackbar.secondActionText = "No"
                            snackbar.secondActionBlock = {snackbar in
                                snackbar.dismiss()
                                let arrayOfTabBarItems = self.tabBarController!.tabBar.items
                                if let barItems = arrayOfTabBarItems, barItems.count > 0 {
                                    for barItem in barItems {
                                        barItem.isEnabled = true
                                    }
                                }
                            }
                        
                            let arrayOfTabBarItems = self.tabBarController!.tabBar.items
                            if let barItems = arrayOfTabBarItems, barItems.count > 0 {
                                for barItem in barItems {
                                    barItem.isEnabled = false
                                }
                            }
                            snackbar.rightMargin = 0
                            snackbar.leftMargin = 0
                            snackbar.bottomMargin = 49
                            snackbar.shouldDismissOnSwipe = false
                            snackbar.show()
                        }
                    }
                } else  {
                    if #available(iOS 10.0, *) {
                        os_log("No message received, %@", log: NevaConstants.logger, type: .error, String(describing: callResult))
                    } else {
                        print("No message received \(callResult)")
                    }
                    let snackbar = TTGSnackbar(message: callResult.statusMessage ?? "UNDEFINED ERROR", duration: .middle)
                    snackbar.backgroundColor = NeVAColors.primaryDarkColor
                    snackbar.shouldDismissOnSwipe = true
                    snackbar.show()
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
    
    func quickAddLikedMealToHistory(mealName: String?)
    {
        if mealName != nil {
            guard let appDelegate =
                UIApplication.shared.delegate as? AppDelegate else {
                    return
            }
            let managedObjectContext =
                appDelegate.databaseContext
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
                    let date = Date().addingTimeInterval(TimeInterval(TimeZone.current.secondsFromGMT()))
                    choice.timestamp.seconds = UInt64(date.timeIntervalSince1970)
                    choice.timestamp.nanos = 0
                    choice.suggesteeID = UInt32(meal.id)
                    request.choice = choice
                    do {
                        let response = try service.informuserchoice(request)
                        if #available(iOS 10.0, *) {
                            os_log("History Entry is registered", log: NevaConstants.logger, type: .info)
                        } else {
                            // Fallback on earlier versions
                            print("History Entry is registered")
                        }
                        let historyEntry = NSEntityDescription.insertNewObject(forEntityName: "HistoryEntry", into: managedObjectContext) as! HistoryEntry
                        historyEntry.choice_id = Int64(response.choiceID)
                        historyEntry.meal = meal
                        historyEntry.date = date
                        historyEntry.userMail = UserToken.email!
                        do {
                            try managedObjectContext.save()
                            NotificationCenter.default.post(name: Notification.Name("reloadHistoryTable"), object: nil)
                        } catch (let error){
                            if #available(iOS 10.0, *) {
                                os_log("Error: %@", log: NevaConstants.logger, type: .fault, String(describing: error))
                            }
                            fatalError("Failed to fetch: \(error)")
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
            } catch {
                if #available(iOS 10.0, *) {
                    os_log("Error: %@", log: NevaConstants.logger, type: .fault, String(describing: error))
                }
                fatalError("Failed to fetch: \(error)")
            }
        }
    }
    func koloda(_ koloda: KolodaView, allowedDirectionsForIndex index: Int) -> [SwipeResultDirection] {
        return [.left, .right]
    }

    func koloda(_ koloda: KolodaView, didSelectCardAt index: Int) {
    }
    func koloda(_ koloda: KolodaView, viewForCardOverlayAt index: Int) -> OverlayView? {
        return nil
    }
    
    @IBOutlet weak var recommendationKolodaView: KolodaView!
    
    func getRecommendationList(_ koloda: KolodaView?) {
        activityIndicator.startAnimating()
        foods = []
        var request = Neva_Backend_GetMultipleSuggestionsRequest()
        request.token = UserToken.token!
        request.suggestionCategory = .meal
        let service = NevaConstants.service
        do {
            let call = try service.getmultiplesuggestions(request) {responseMessage, callResult in
                DispatchQueue.main.async {
                    self.activityIndicator.stopAnimating()
                    if let responseMessage = responseMessage {
                        if #available(iOS 10.0, *) {
                            os_log("Recommendation list is received %@ ", log: NevaConstants.logger, type: .info, String(describing: responseMessage.suggestion.suggestionList.map({$0.suggesteeID})))
                        } else {
                            print("Recommendation list is received \(responseMessage.suggestion.suggestionList.map({$0.suggesteeID}))")
                        }
                        for suggestedItem in responseMessage.suggestion.suggestionList {
                            if let meal = self.getMeal(with: Int(suggestedItem.suggesteeID)) {
                                self.foods.append(meal)
                            }
                        }
                        koloda?.resetCurrentCardIndex()
                    } else  {
                        if #available(iOS 10.0, *) {
                            os_log("No message received, %@", log: NevaConstants.logger, type: .error, String(describing: callResult))
                        } else {
                            print("No message received \(callResult)")
                        }
                        let snackbar = TTGSnackbar(message: callResult.statusMessage ?? "UNDEFINED ERROR", duration: .middle)
                        snackbar.backgroundColor = NeVAColors.primaryDarkColor
                        snackbar.shouldDismissOnSwipe = true
                        snackbar.show()
                    }
                }
            }
        } catch (let error){
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
    
    override func viewDidLoad() {
        super.viewDidLoad()
        recommendationKolodaView.delegate = self
        recommendationKolodaView.dataSource = self
        getRecommendationList(recommendationKolodaView)

        // Do any additional setup after loading the view.
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    func getMeal(with id: Int) -> Meal? {
        guard let appDelegate =
            UIApplication.shared.delegate as? AppDelegate else {
                return nil
        }
        let managedObjectContext =
            appDelegate.databaseContext
        let fetchRequest = NSFetchRequest<NSFetchRequestResult>(entityName: "Meal")
        fetchRequest.predicate = NSPredicate(format: "id==%@", argumentArray: [id])
        do {
            let fetchedEntries = try managedObjectContext.fetch(fetchRequest) as? [Meal]
            if let meal = fetchedEntries, !meal.isEmpty {
                return meal[0]
            }
            return nil
        } catch {
            if #available(iOS 10.0, *) {
                os_log("Error: %@", log: NevaConstants.logger, type: .fault, String(describing: error))
            }
            fatalError("Failed to fetch: \(error)")
        }
    }
    func getLastChoiceId() -> UInt32 {
        let appDelegate = UIApplication.shared.delegate as! AppDelegate
        let managedObjectContext = appDelegate.databaseContext
        let fetchRequest = NSFetchRequest<NSFetchRequestResult>(entityName: "HistoryEntry")
        let sortDescriptor = NSSortDescriptor(key: "choice_id", ascending: false)
        fetchRequest.sortDescriptors = [sortDescriptor]
        fetchRequest.fetchLimit = 1
        let predicate = NSPredicate(format: "(userMail == %@)", argumentArray: [UserToken.email!])
        fetchRequest.predicate = predicate
        do {
            let fetchedEntries = try managedObjectContext.fetch(fetchRequest) as! [HistoryEntry]
            if !fetchedEntries.isEmpty {
                return UInt32(fetchedEntries[0].choice_id)
           }
        } catch (let error) {
            if #available(iOS 10.0, *) {
                os_log("Error: %@", log: NevaConstants.logger, type: .fault, String(describing: error))
            }
            fatalError("Failed to fetch: \(error)")
        }
        return 0
    }
}
