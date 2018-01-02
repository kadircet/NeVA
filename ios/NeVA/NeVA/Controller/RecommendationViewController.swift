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
        view.image.image = UIImage(named: "loginRegisterViewBackground")
        return view
    }
    
    
    func kolodaNumberOfCards(_ koloda: KolodaView) -> Int {
        return foods.count
    }
    
    func kolodaSpeedThatCardShouldDrag(_ koloda: KolodaView) -> DragSpeed {
        return .fast
    }
    
    func kolodaDidRunOutOfCards(_ koloda: KolodaView) {
        getRecommendationList()
        koloda.resetCurrentCardIndex()
    }
    
    func koloda(_ koloda: KolodaView, didSwipeCardAt index: Int, in direction: SwipeResultDirection) {
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
        
        let service = NevaConstants.service
        do {
            let response = try service.recordfeedback(request)
            print(response)
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
    
    func getRecommendationList() {
        foods = []
        var request = Neva_Backend_GetMultipleSuggestionsRequest()
        request.token = UserToken.token!
        request.suggestionCategory = .meal
        let service = NevaConstants.service
        do {
            //TODO: DO IT ASYNC
            let reply = try service.getmultiplesuggestions(request)
            print(reply)
            for suggestedItem in reply.suggestion.suggestionList {
                if let meal = getMeal(with: Int(suggestedItem.suggesteeID)) {
                    foods.append(meal)
                }
            }
        } catch (let error){
            print(error)
        }
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        getRecommendationList()
        recommendationKolodaView.delegate = self
        recommendationKolodaView.dataSource = self
        
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
