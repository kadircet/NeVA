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
        print(request)
        let service = NevaConstants.service
        do {
            let call = try service.recordfeedback(request) {responseMessage, callResult in
                //print("recordfeedback callback start")
                if let responseMessage = responseMessage {
                    print(responseMessage)
                } else  {
                    print("No message received. \(callResult)")
                }
                //print("recordfeedback callback end")
            }
            //print(call)
        } catch (let error) {
            print(error)
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
                //print("getmultiplesuggestion callback start")
                DispatchQueue.main.async {
                    self.activityIndicator.stopAnimating()
                    if let responseMessage = responseMessage {
                        for suggestedItem in responseMessage.suggestion.suggestionList {
                            if let meal = self.getMeal(with: Int(suggestedItem.suggesteeID)) {
                                self.foods.append(meal)
                            }
                        }
                        koloda?.resetCurrentCardIndex()
                    } else  {
                        print("No message received. \(callResult)")
                    }
                }
                //print("getmultiplesuggestion callback end")
            }
            //print(call)
        } catch (let error){
            print(error)
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
           fatalError("Failed to fetch: \(error)")
        }
        return 0
    }
}
