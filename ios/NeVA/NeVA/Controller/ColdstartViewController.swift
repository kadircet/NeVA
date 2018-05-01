//
//  ColdstartViewController.swift
//  NeVA
//
//  Created by Bilal Yaylak on 2.05.2018.
//  Copyright © 2018 Bilemiyorum Altan. All rights reserved.
//

import UIKit
import Koloda
import CoreData
import TTGSnackbar
import os


class ColdstartViewController: UIViewController, KolodaViewDelegate, KolodaViewDataSource {
  
  @IBOutlet weak var remainingNumber: UILabel!
  
  @IBAction func pressedDislike(_ sender: Any) {
    coldstartKolodaView.swipe(.left)
  }
  @IBAction func pressedLike(_ sender: Any) {
    coldstartKolodaView.swipe(.right)
  }
  func kolodaDidRunOutOfCards(_ koloda: KolodaView) {
    completeColdStart()
  }
  func kolodaSpeedThatCardShouldDrag(_ koloda: KolodaView) -> DragSpeed {
    return .fast
  }
  func koloda(_ koloda: KolodaView, didSwipeCardAt index: Int, in direction: SwipeResultDirection) {
    var request = Neva_Backend_RecordColdStartChoiceRequest()
    request.token = UserToken.token!
    switch direction {
    case .left:
      request.feedback = .dislike
    case .right:
      request.feedback = .like
    default:
      request.feedback = .like
    }
    request.coldstartItem.suggesteeID = UInt32(foods[index].id)
    let service = NevaConstants.service
    do {
      let call = try service.recordcoldstartchoice(request) {responseMessage, callResult in
        if responseMessage != nil {
          if #available(iOS 10.0, *) {
            os_log("Coldstart Record is registered.", log: NevaConstants.logger, type: .info)
          } else {
            print("Coldstart Record is registered.")
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
    } catch(let error) {
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
    remainingNumber.text = "\(foods.count-koloda.currentCardIndex) Left"
  }
  @IBOutlet weak var activityIndicator: UIActivityIndicatorView!
  @IBOutlet weak var coldstartKolodaView: KolodaView!
  
  var foods: [Meal] = []
  
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
  
  func koloda(_ koloda: KolodaView, allowedDirectionsForIndex index: Int) -> [SwipeResultDirection] {
    return [.left, .right]
  }
  
  func koloda(_ koloda: KolodaView, didSelectCardAt index: Int) {
  }
  func koloda(_ koloda: KolodaView, viewForCardOverlayAt index: Int) -> OverlayView? {
    return nil
  }
  func kolodaNumberOfCards(_ koloda: KolodaView) -> Int {
    return foods.count
  }
  
  var completedColdstart = false
  override func viewDidLoad() {
      super.viewDidLoad()
      coldstartKolodaView.delegate = self
      coldstartKolodaView.dataSource = self
      if(!completedColdstart) {
        getColdstartList(coldstartKolodaView)
      }
      // Do any additional setup after loading the view.
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
  
    override func viewDidAppear(_ animated: Bool) {
      if(completedColdstart) {
        dismiss(animated: false)
      }
    }
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
  
    func completeColdStart() {
      completedColdstart = true;
      performSegue(withIdentifier: "continueToNeva", sender: self)
    }
  
    func getColdstartList(_ koloda: KolodaView?) {
      self.remainingNumber.text = ""
      activityIndicator.startAnimating()
      foods = []
      var request = Neva_Backend_GetColdStartItemListRequest()
      request.token = UserToken.token!
      request.coldstartItemCategory = .meal
      let service = NevaConstants.service
      do {
        let call = try service.getcoldstartitemlist(request) {responseMessage, callResult in
          DispatchQueue.main.async {
            self.activityIndicator.stopAnimating()
            if let responseMessage = responseMessage {
              if #available(iOS 10.0, *) {
                os_log("Coldstart list is received %@ ", log: NevaConstants.logger, type: .info, String(describing: responseMessage.coldstartItemList.suggestionList.map({$0.suggesteeID})))
              } else {
                print("Coldstart list is received \(responseMessage.coldstartItemList.suggestionList.map({$0.suggesteeID}))")
              }
              for suggestedItem in responseMessage.coldstartItemList.suggestionList {
                if let meal = self.getMeal(with: Int(suggestedItem.suggesteeID)) {
                  self.foods.append(meal)
                }
              }
              koloda!.resetCurrentCardIndex()
              self.remainingNumber.text = "\(self.foods.count-koloda!.currentCardIndex) Left"
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
    /*
    // MARK: - Navigation

    // In a storyboard-based application, you will often want to do a little preparation before navigation
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        // Get the new view controller using segue.destinationViewController.
        // Pass the selected object to the new view controller.
    }
    */
  
}
