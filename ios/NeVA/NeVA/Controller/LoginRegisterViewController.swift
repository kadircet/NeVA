//
//  LoginRegisterViewController.swift
//  NeVA
//
//  Created by Bilal Yaylak on 6.11.2017.
//  Copyright © 2017 Bilemiyorum Altan. All rights reserved.
//

import UIKit
import FBSDKLoginKit
class LoginRegisterViewController: UIViewController, FBSDKLoginButtonDelegate, UITextFieldDelegate {
    
    //UITextFieldDelegate functions Start
    func textFieldDidEndEditing(_ textField: UITextField) {
        if textField == emailField {
            loginEmail = textField.text
        } else if textField == passwordField {
            loginPassword = textField.text
        }
    }
    //UITextFieldDelegate functions End
    
    //FBDKLoginButtonDelegate Functions Start
    func loginButton(_ loginButton: FBSDKLoginButton!, didCompleteWith result: FBSDKLoginManagerLoginResult!, error: Error!) {
        if ((error) != nil) {
            print("Error: \(error!)")
        } else {
            if(!result.isCancelled || result.token != nil)
            {
                performSegue(withIdentifier: "loggedIn", sender: self)
            }
        }
    }
    
    func loginButtonDidLogOut(_ loginButton: FBSDKLoginButton!) {
    }
    
    func loginButtonWillLogin(_ loginButton: FBSDKLoginButton!) -> Bool {
        return true
    }
    //FBDKLoginButtonDelegate Functions End
    @IBAction func loginButtonPressed(_ sender: Any) {
        var willLogin = true
        
        if let email = loginEmail as String!, !email.isEmpty{
            //TODO_BILAL: input sanitization
        } else {
            emailField.shake()
            willLogin = false
        }
        
        if let password = loginPassword as String!, !password.isEmpty {
            //TODO_BILAL: input sanitization
        } else {
            passwordField.shake()
            willLogin = false
        }
        
        if !willLogin {
            return
        }
    
        var loginRequestMessage = Neva_Backend_LoginRequest()
        loginRequestMessage.email = loginEmail!
        loginRequestMessage.password = loginPassword!
        print(loginRequestMessage)
        let service = Neva_Backend_BackendService.init(address: "0xdeffbeef.com:50051")
        do {
            let responseMessage = try service.login(loginRequestMessage)
            print(responseMessage)
            performSegue(withIdentifier: "loggedIn", sender: self)
        } catch (let error) {
            print(error)
            emailField.shake()
            passwordField.shake()
        }
    }
    
    //MARK: Properties
    let facebookLoginButton : FBSDKLoginButton = {
        let button = FBSDKLoginButton()
        button.readPermissions = ["email"]
        return button
    }()
    
    
    @IBOutlet weak var emailField: LoginRegisterInputField!
    @IBOutlet weak var passwordField: LoginRegisterInputField!
    var loginEmail: String?
    var loginPassword: String?
    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view.
        
        let tap = UITapGestureRecognizer(target: self.view, action: #selector(UIView.endEditing(_:)))
        tap.cancelsTouchesInView = false
        self.view.addGestureRecognizer(tap)
        
        view.addSubview(facebookLoginButton)
        facebookLoginButton.delegate = self
        facebookLoginButton.translatesAutoresizingMaskIntoConstraints = false
        for view in view.subviews {
            if (view.restorationIdentifier == "passwordRegisterStack") {
                /*let heightConstraint = NSLayoutConstraint(item: facebookLoginButton, attribute: NSLayoutAttribute.height, relatedBy: NSLayoutRelation.equal, toItem: nil, attribute: NSLayoutAttribute.notAnAttribute, multiplier: 1.0, constant: 30.0)*/
                let leadingConstraint = NSLayoutConstraint(item: facebookLoginButton, attribute: NSLayoutAttribute.leading, relatedBy: NSLayoutRelation.equal, toItem: self.view, attribute: NSLayoutAttribute.leading, multiplier: 1.0, constant: 60.0)
                let trailingConstraint = NSLayoutConstraint(item: facebookLoginButton, attribute: NSLayoutAttribute.trailing, relatedBy: NSLayoutRelation.equal, toItem: self.view, attribute: NSLayoutAttribute.trailing, multiplier: 1.0, constant: -60.0)
                let topConstraint =  NSLayoutConstraint(item: facebookLoginButton, attribute: NSLayoutAttribute.top, relatedBy: NSLayoutRelation.equal, toItem: view, attribute: NSLayoutAttribute.bottom, multiplier: 1.0, constant: 5.0)
                NSLayoutConstraint.activate([leadingConstraint,trailingConstraint,topConstraint])
            }
        }
        
        emailField.delegate = self
        passwordField.delegate = self
        
    }
    
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        if FBSDKAccessToken.current() != nil {
            performSegue(withIdentifier: "loggedIn", sender: self)
        }
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    

    /*
    // MARK: - Navigation

    // In a storyboard-based application, you will often want to do a little preparation before navigation
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        // Get the new view controller using segue.destinationViewController.
        // Pass the selected object to the new view controller.
    
    }*/
    

}
