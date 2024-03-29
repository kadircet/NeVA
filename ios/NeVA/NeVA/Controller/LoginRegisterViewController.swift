//
//  LoginRegisterViewController.swift
//  NeVA
//
//  Created by Bilal Yaylak on 6.11.2017.
//  Copyright © 2017 Bilemiyorum Altan. All rights reserved.
//

import UIKit
import FBSDKLoginKit
import TTGSnackbar
import os

class LoginRegisterViewController: UIViewController, FBSDKLoginButtonDelegate, UITextFieldDelegate {
    
    //UITextFieldDelegate functions Start
    func textFieldDidEndEditing(_ textField: UITextField) {
        switch textField {
        case loginEmailField:
            loginEmail = textField.text
        case loginPasswordField:
            loginPassword = textField.text
        case registerEmailField:
            registerEmail = textField.text
        case registerNameField:
            registerName = textField.text
        case registerPasswordField:
            registerPassword = textField.text
        case registerConfirmPasswordField:
            registerConfirmPassword = textField.text
            if registerPassword != nil, registerConfirmPassword != nil, !registerConfirmPassword!.isEmpty ,registerPassword == registerConfirmPassword {
                textField.layer.borderColor = UIColor(red: 104.0/255.0, green: 159.0/255.0, blue: 56.0/255.0, alpha: 1.0).cgColor
            } else {
                textField.layer.borderColor = UIColor.red.cgColor
            }
        default:
            print("What?")
        }
    }
    
    func textFieldShouldReturn(_ textField: UITextField) -> Bool {
        self.view.endEditing(true)
        return false
    }
    //UITextFieldDelegate functions End
    
    //FBDKLoginButtonDelegate Functions Start
    func loginButton(_ loginButton: FBSDKLoginButton!, didCompleteWith result: FBSDKLoginManagerLoginResult!, error: Error!) {
        if ((error) != nil) {
            if #available(iOS 10.0, *) {
                os_log("Error: %@", log: NevaConstants.logger, type: .error, String(describing: error))
            } else {
                // Fallback on earlier versions
                print("Error: \(error!)")
            }
        } else {
            if(!result.isCancelled || result.token != nil)
            {
                var loginRequestMessage = Neva_Backend_LoginRequest()
                loginRequestMessage.email = result.token.userID
                loginRequestMessage.password = result.token.tokenString
                loginRequestMessage.authenticationType = .facebook
                let service = NevaConstants.service
                do {
                    let responseMessage = try service.login(loginRequestMessage)
                    UserToken.token = responseMessage.token
                    UserToken.email = result.token.userID
                    UserToken.type = .facebook
                    if #available(iOS 10.0, *) {
                        os_log("User %@ successfully logged in.", log: NevaConstants.logger, type: .info, String(describing: UserToken.email))
                    } else {
                        print("User \(String(describing: UserToken.email)) successfully logged in.")
                    }
                    loginAndCheckColdstartStatus()
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
                    FBSDKLoginManager().logOut()
                }
            }
        }
    }
    
    func loginButtonDidLogOut(_ loginButton: FBSDKLoginButton!) {
    }
    
    func loginButtonWillLogin(_ loginButton: FBSDKLoginButton!) -> Bool {
        return true
    }
    //FBDKLoginButtonDelegate Functions End
    @IBAction func registerButtonPressed(_ sender: Any) {
        var willRegister = true
        
        if let email = registerEmail as String!, !email.isEmpty {
            //TODO_BILAL: input sanitization
        } else {
            registerEmailField.shake()
            willRegister = false
        }
        
        if let name = registerName as String!, !name.isEmpty {
            //TODO_BILAL: input sanitization
        } else {
            registerNameField.shake()
            willRegister = false
        }
        
        if let password = registerPassword as String!, !password.isEmpty {
            //TODO_BILAL: input sanitization
        } else {
            registerPasswordField.shake()
            willRegister = false
        }
        
        if let confirmPassword = registerConfirmPassword as String!, !confirmPassword.isEmpty {
            //TODO_BILAL: input sanitization
        } else {
            registerConfirmPasswordField.shake()
            willRegister = false
        }
        
        if registerPassword != nil, registerConfirmPassword != nil ,registerPassword == registerConfirmPassword {
            //TODO_BILAL: input sanitization
        } else {
            registerConfirmPasswordField.shake()
            willRegister = false
        }
        
        if !willRegister {
            return
        }
        
        var user = Neva_Backend_User()
        user.email = registerEmail!
        user.password = registerPassword!
        user.name = registerName!
        user.dateOfBirth.seconds = UInt64(registerBirthday.timeIntervalSince1970)
        user.gender = registerGender
        var requestMessage = Neva_Backend_RegisterRequest()
        requestMessage.user = user
    
        
        let service = NevaConstants.service
        do {
            _ = try service.register(requestMessage)
            loginView.isHidden = false
            registerView.isHidden = true
            registerEmail = ""
            registerEmailField.text = ""
            registerName = ""
            registerNameField.text = ""
            genderPicker.selectedSegmentIndex = 0
            registerGender = .male
            birthdayPicker.date = birthdayPicker.maximumDate ?? Date()
            registerPassword = ""
            registerPasswordField.text = ""
            registerConfirmPassword = ""
            registerConfirmPasswordField.text = ""
            registerConfirmPasswordField.layer.borderColor = UIColor.white.cgColor
            if #available(iOS 10.0, *) {
                os_log("User %@ successfully registered.", log: NevaConstants.logger, type: .info, String(describing: user.email))
            } else {
                print("User \(String(describing: user.email)) successfully registered.")
            }
        } catch(let error) {
            registerEmailField.shake()
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
    
    
    @IBAction func loginButtonPressed(_ sender: Any) {
        var willLogin = true
        
        if let email = loginEmail as String!, !email.isEmpty {
            //TODO_BILAL: input sanitization
        } else {
            loginEmailField.shake()
            willLogin = false
        }
        
        if let password = loginPassword as String!, !password.isEmpty {
            //TODO_BILAL: input sanitization
        } else {
            loginPasswordField.shake()
            willLogin = false
        }
        
        if !willLogin {
            return
        }
    
        var loginRequestMessage = Neva_Backend_LoginRequest()
        loginRequestMessage.email = loginEmail!
        loginRequestMessage.password = loginPassword!
        loginRequestMessage.authenticationType = .default
        let service = NevaConstants.service
        do {
            let responseMessage = try service.login(loginRequestMessage)
            UserToken.setUserToken(email: loginRequestMessage.email, token: responseMessage.token)
            if #available(iOS 10.0, *) {
                os_log("User %@ successfully logged in.", log: NevaConstants.logger, type: .info, String(describing: UserToken.email))
            } else {
                print("User \(String(describing: UserToken.email)) successfully logged in.")
            }
            loginEmail = ""
            loginEmailField.text = ""
            loginPassword = ""
            loginPasswordField.text = ""
            loginAndCheckColdstartStatus()
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
            loginEmailField.shake()
            loginPasswordField.shake()
        }
    }
    @IBAction func backButtonPressed(_ sender: Any) {
        loginView.isHidden = false
        registerView.isHidden = true
    }
    @IBAction func registerScreenButtonPressed(_ sender: Any) {
        loginView.isHidden = true
        registerView.isHidden = false
    }
    
    @IBAction func genderChanged(_ sender: UISegmentedControl) {
        switch genderPicker.selectedSegmentIndex {
        case 0:
            registerGender = .male
        case 1:
            registerGender = .female
        default:
            break
        }
    }
    @IBAction func dateChanged(_ sender: UIDatePicker) {
        registerBirthday = sender.date
    }
    
    
    //MARK: Properties
    let facebookLoginButton : FBSDKLoginButton = {
        let button = FBSDKLoginButton()
        button.readPermissions = ["email"]
        return button
    }()
    
    //Login Screen Elements
    @IBOutlet weak var loginView: UIView!
    @IBOutlet weak var loginEmailField: UITextField!
    @IBOutlet weak var loginPasswordField: UITextField!
    
    //
    
    //Register Screen Elements
    @IBOutlet weak var registerView: UIView!
    @IBOutlet weak var registerEmailField: UITextField!
    @IBOutlet weak var registerNameField: UITextField!
    @IBOutlet weak var birthdayPicker: UIDatePicker!
    @IBOutlet weak var genderPicker: UISegmentedControl!
    @IBOutlet weak var registerPasswordField: UITextField!
    @IBOutlet weak var registerConfirmPasswordField: UITextField!
    //
    
    
    var loginEmail: String? = ""
    var loginPassword: String? = ""
    
    var registerEmail: String? = ""
    var registerName: String? = ""
    var registerGender: Neva_Backend_User.Gender = .male
    var registerBirthday : Date = Date()
    var registerPassword: String? = ""
    var registerConfirmPassword: String? = ""
    
    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view.
        
        let tap = UITapGestureRecognizer(target: self.view, action: #selector(UIView.endEditing(_:)))
        tap.cancelsTouchesInView = false
        self.view.addGestureRecognizer(tap)
        
        
        for loginview in view.subviews {
            if (loginview.restorationIdentifier == "Login View") {
                loginview.addSubview(facebookLoginButton)
                facebookLoginButton.delegate = self
                facebookLoginButton.translatesAutoresizingMaskIntoConstraints = false
                for view in loginview.subviews {
                    if (view.restorationIdentifier == "passwordRegisterStack") {
                        let leadingConstraint = NSLayoutConstraint(item: facebookLoginButton, attribute: NSLayoutAttribute.leading, relatedBy: NSLayoutRelation.equal, toItem: self.view, attribute: NSLayoutAttribute.leading, multiplier: 1.0, constant: 60.0)
                        let trailingConstraint = NSLayoutConstraint(item: facebookLoginButton, attribute: NSLayoutAttribute.trailing, relatedBy: NSLayoutRelation.equal, toItem: self.view, attribute: NSLayoutAttribute.trailing, multiplier: 1.0, constant: -60.0)
                        let topConstraint =  NSLayoutConstraint(item: facebookLoginButton, attribute: NSLayoutAttribute.top, relatedBy: NSLayoutRelation.equal, toItem: view, attribute: NSLayoutAttribute.bottom, multiplier: 1.0, constant: 5.0)
                        NSLayoutConstraint.activate([leadingConstraint,trailingConstraint,topConstraint])
                        break
                    }
                }
            break
            }
        }
        //Login elements init
        loginView.isHidden = false
        loginEmailField.delegate = self
        loginPasswordField.delegate = self
        //
        
        //Register elements init
        registerView.isHidden = true
        registerEmailField.delegate = self
        registerNameField.delegate = self
        registerPasswordField.delegate = self
        registerConfirmPasswordField.delegate = self
        registerBirthday = birthdayPicker.date
        //

    }
    
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        if FBSDKAccessToken.current() != nil {
          var loginRequestMessage = Neva_Backend_LoginRequest()
          loginRequestMessage.email = FBSDKAccessToken.current().userID
          loginRequestMessage.password = FBSDKAccessToken.current().tokenString
          loginRequestMessage.authenticationType = .facebook
          let service = NevaConstants.service
          do {
            let responseMessage = try service.login(loginRequestMessage)
            UserToken.token = responseMessage.token
            UserToken.email = FBSDKAccessToken.current().userID
            UserToken.type = .facebook
            if #available(iOS 10.0, *) {
              os_log("User %@ successfully logged in.", log: NevaConstants.logger, type: .info, String(describing: UserToken.email))
            } else {
              print("User \(String(describing: UserToken.email)) successfully logged in.")
            }
            loginAndCheckColdstartStatus()
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
            FBSDKLoginManager().logOut()
          }
        } else if UserToken.initializeToken() {
          let service = NevaConstants.service
          var checkTokenRequest = Neva_Backend_CheckTokenRequest()
          checkTokenRequest.token = UserToken.token!
          do {
            let _ = try service.checktoken(checkTokenRequest)
            loginAndCheckColdstartStatus()
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
            UserToken.clearUserToken()
          }
        }
      }
  
      func loginAndCheckColdstartStatus() {
        let service = NevaConstants.service
        var getColdstartCompletionStatusRequest = Neva_Backend_GetColdStartCompletionStatusRequest()
        getColdstartCompletionStatusRequest.token = UserToken.token!
        do {
          let response = try service.getcoldstartcompletionstatus(getColdstartCompletionStatusRequest)
          if(response.completionStatus) {
            performSegue(withIdentifier: "loggedInCompletedColdstart", sender: self)
          } else {
            performSegue(withIdentifier: "loggedInWithoutColdstart", sender: self)
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
            UserToken.clearUserToken()
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
