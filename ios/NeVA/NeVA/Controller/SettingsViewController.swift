//
//  SettingsViewController.swift
//  NeVA
//
//  Created by Bilal Yaylak on 28.11.2017.
//  Copyright © 2017 Bilemiyorum Altan. All rights reserved.
//

import UIKit
import FBSDKLoginKit

class SettingsViewController: UIViewController, FBSDKLoginButtonDelegate {
   
    func loginButton(_ loginButton: FBSDKLoginButton!, didCompleteWith result: FBSDKLoginManagerLoginResult!, error: Error!) {
        
    }
    
    func loginButtonDidLogOut(_ loginButton: FBSDKLoginButton!) {
        if let tabBarVC = tabBarController
        {
            UserToken.token = nil
            UserToken.email = nil
            tabBarVC.dismiss(animated: true, completion: nil)
        }
    }
    
    func loginButtonWillLogin(_ loginButton: FBSDKLoginButton!) -> Bool {
        return false
    }
    @IBAction func logoutButtonPressed(_ sender: Any) {
        if let tabBarVC = tabBarController
        {
            UserToken.token = nil
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
        if let user_token_type = UserToken.type, user_token_type == .facebook {
            view.addSubview(facebookLogoutButton)
            facebookLogoutButton.delegate = self
            facebookLogoutButton.center = view.center
            logoutButton.isHidden = true
        } 
        // Do any additional setup after loading the view.
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
    }
    */

}
