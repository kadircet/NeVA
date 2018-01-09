//
//  SettingsViewController.swift
//  NeVA
//
//  Created by Bilal Yaylak on 28.11.2017.
//  Copyright © 2017 Bilemiyorum Altan. All rights reserved.
//

import UIKit
import FBSDKLoginKit

class SettingsViewController: UIViewController, FBSDKLoginButtonDelegate, UITextFieldDelegate {
    var user : Neva_Backend_User? = nil
    
    @IBOutlet weak var nameField: UITextField!
    @IBOutlet weak var weightField: UITextField!
    @IBOutlet weak var genderPicker: UISegmentedControl!
    @IBOutlet weak var birthdatePicker: UIDatePicker!
    
    func textFieldShouldReturn(_ textField: UITextField) -> Bool {
        self.view.endEditing(true)
        return false
    }
    
    @IBAction func updateButtonPressed(_ sender: Any) {
        if user == nil {
            //TODO: toast error
            return
        }
        //No deepcopy 
        let oldName = user!.name
        let oldWeight = user!.weight
        let oldDate = user!.dateOfBirth
        let oldGender = user!.gender
        
        var nameWillBeSent: String
        var weightWillBeSent: Float
        if let name = nameField.text, !name.isEmpty {
            nameWillBeSent = name
        } else {
            nameField.shake()
            return
        }
        if let weightText = weightField.text, let nsweight = NumberFormatter().number(from: weightText), nsweight.floatValue >= 0.0 {
            weightWillBeSent = nsweight.floatValue
        } else {
            weightField.shake()
            return
        }
        let dateWillBeSent = birthdatePicker.date
        user!.name = nameWillBeSent
        user!.weight = weightWillBeSent
        var timeStampWillBeSent = Neva_Backend_Util_Timestamp()
        timeStampWillBeSent.seconds = UInt64(dateWillBeSent.timeIntervalSince1970)
        user!.dateOfBirth = timeStampWillBeSent
        if genderPicker.selectedSegmentIndex == 0 {
            user!.gender = .male
        } else {
            user!.gender = .female
        }
        
        var request = Neva_Backend_UpdateUserRequest()
        request.token = UserToken.token!
        request.user = user!
        let service = NevaConstants.service
        do {
            let response = try service.updateuser(request)
            print(response)
        } catch (let error) {
            user!.dateOfBirth = oldDate
            user!.name = oldName
            user!.gender = oldGender
            user!.weight = oldWeight
            print(error)
        }
    }
    func getUserData() {
        var request = Neva_Backend_GetUserRequest()
        request.token = UserToken.token!
        let service = NevaConstants.service
        do {
            let response = try service.getuser(request)
            user = response.user
            nameField.text = user!.name
            weightField.text = "\(user!.weight)"
            let birthdate = user!.dateOfBirth
            birthdatePicker.date = Date(timeIntervalSince1970: Double(birthdate.seconds)+Double(birthdate.nanos))
            switch user!.gender {
            case .male:
                genderPicker.selectedSegmentIndex = 0
            case .female:
                genderPicker.selectedSegmentIndex = 1
            case .invalidGender:
                print("invalid gender received")
            case .UNRECOGNIZED(_):
                print("error")
            }
        } catch (let error) {
            print(error)
        }
    }
    func loginButton(_ loginButton: FBSDKLoginButton!, didCompleteWith result: FBSDKLoginManagerLoginResult!, error: Error!) {
        
    }
    
    func loginButtonDidLogOut(_ loginButton: FBSDKLoginButton!) {
        if let tabBarVC = tabBarController
        {
            UserToken.clearUserToken()
            tabBarVC.dismiss(animated: true, completion: nil)
        }
    }
    
    func loginButtonWillLogin(_ loginButton: FBSDKLoginButton!) -> Bool {
        return false
    }
    @IBAction func logoutButtonPressed(_ sender: Any) {
        if let tabBarVC = tabBarController
        {
            UserToken.clearUserToken()
            tabBarVC.dismiss(animated: true, completion: nil)
        }
    }
    
    let facebookLogoutButton : FBSDKLoginButton = {
        let button = FBSDKLoginButton()
        button.readPermissions = ["email"]
        return button
    }()
    @IBOutlet weak var logoutButton: UIButton!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        let tap = UITapGestureRecognizer(target: self.view, action: #selector(UIView.endEditing(_:)))
        tap.cancelsTouchesInView = false
        self.view.addGestureRecognizer(tap)
        
        nameField.delegate = self
        weightField.delegate = self
        if let user_token_type = UserToken.type, user_token_type == .facebook {
            //Since these values are gained from facebook
            nameField.isUserInteractionEnabled = false
            birthdatePicker.isUserInteractionEnabled = false
            genderPicker.isUserInteractionEnabled = false
            view.addSubview(facebookLogoutButton)
            facebookLogoutButton.delegate = self
            facebookLogoutButton.translatesAutoresizingMaskIntoConstraints = false
            let leadingConstraint = NSLayoutConstraint(item: facebookLogoutButton, attribute: NSLayoutAttribute.leading, relatedBy: NSLayoutRelation.equal, toItem: logoutButton, attribute: NSLayoutAttribute.leading, multiplier: 1.0, constant: 0.0)
            let trailingConstraint = NSLayoutConstraint(item: facebookLogoutButton, attribute: NSLayoutAttribute.trailing, relatedBy: NSLayoutRelation.equal, toItem: logoutButton, attribute: NSLayoutAttribute.trailing, multiplier: 1.0, constant: 0.0)
            let topConstraint =  NSLayoutConstraint(item: facebookLogoutButton, attribute: NSLayoutAttribute.top, relatedBy: NSLayoutRelation.equal, toItem: logoutButton, attribute: NSLayoutAttribute.top, multiplier: 1.0, constant: 0.0)
            let bottomConstraint = NSLayoutConstraint(item: facebookLogoutButton, attribute: NSLayoutAttribute.bottom, relatedBy: NSLayoutRelation.equal, toItem: logoutButton, attribute: NSLayoutAttribute.bottom, multiplier: 1.0, constant: 0.0)
            NSLayoutConstraint.activate([leadingConstraint,trailingConstraint,topConstraint,bottomConstraint])
            logoutButton.isHidden = true
        }
        getUserData()
        // Do any additional setup after loading the view.
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
}
