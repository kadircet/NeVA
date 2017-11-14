//
//  TempViewController.swift
//  NeVA
//
//  Created by Bilal Yaylak on 7.11.2017.
//  Copyright © 2017 Bilemiyorum Altan. All rights reserved.
//

import UIKit
import FBSDKLoginKit
import gRPC

class TempViewController: UIViewController, FBSDKLoginButtonDelegate {
    func loginButton(_ loginButton: FBSDKLoginButton!, didCompleteWith result: FBSDKLoginManagerLoginResult!, error: Error!) {
        
    }
    
    func loginButtonDidLogOut(_ loginButton: FBSDKLoginButton!) {
        dismiss(animated: true, completion: nil)
    }
    
    func loginButtonWillLogin(_ loginButton: FBSDKLoginButton!) -> Bool {
        return false
    }
    
    var emailText: String?;
    let facebookLogoutButton : FBSDKLoginButton = {
        let button = FBSDKLoginButton()
        button.readPermissions = ["email"]
        return button
    }()
    @IBOutlet weak var emailLabel: UILabel?
   
    override func viewDidLoad() {
        super.viewDidLoad()
        if FBSDKAccessToken.current() != nil {
            let parameters = ["fields": "email"]
            FBSDKGraphRequest(graphPath: "me", parameters: parameters).start(completionHandler: { (connection, result, error) -> Void in
                if ((error) != nil) {
                    print("Error: \(error!)")
                } else {
                    if let data = result as? [String:Any] {
                        if let email = data["email"] as? String {
                            self.emailLabel!.text = email
                            print(email)
                        }
                    }
                }
            })
        }
        view.addSubview(facebookLogoutButton)
        facebookLogoutButton.delegate = self
        facebookLogoutButton.center = view.center
        
        //Checking if requests are working
        let requestMessage = Backend_RegisterRequest()
        let service = Backend_BackendService.init(address: "0xdeffbeef.com:50051");
        do {
            _ = try service.register(requestMessage)
        } catch(let error){
            print("ERROROROROR")
            print(error)
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
