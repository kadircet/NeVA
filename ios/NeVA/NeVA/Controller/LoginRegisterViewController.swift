//
//  LoginRegisterViewController.swift
//  NeVA
//
//  Created by Bilal Yaylak on 6.11.2017.
//  Copyright © 2017 Bilemiyorum Altan. All rights reserved.
//

import UIKit
import FBSDKLoginKit
class LoginRegisterViewController: UIViewController, FBSDKLoginButtonDelegate {
    
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
    
    let facebookLoginButton : FBSDKLoginButton = {
        let button = FBSDKLoginButton()
        button.readPermissions = ["email"]
        return button
    }()
    
    var email: String?
    
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
