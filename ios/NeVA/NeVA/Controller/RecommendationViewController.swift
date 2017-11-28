//
//  RecommendationViewController.swift
//  NeVA
//
//  Created by Bilal Yaylak on 28.11.2017.
//  Copyright © 2017 Bilemiyorum Altan. All rights reserved.
//

import UIKit

class RecommendationViewController: UIViewController {
    
    @IBOutlet weak var recommendationView: UIView!
    
    @IBOutlet weak var recommendationImage: UIImage!
    
    @IBOutlet weak var recommendationName: UILabel!
    
    @IBAction func getRecommendation(_ sender: Any) {
        activityIndicator.startAnimating()
        recommendationView.isHidden = false
        if let button = sender as? UIButton {
            button.isHidden = true
        }
        
        var request = Neva_Backend_GetMealSuggestionRequest()
        request.token = UserToken.token!
        let service = Neva_Backend_BackendService.init(address: "0xdeffbeef.com:50051")
        do {
            _ = try service.getmealsuggestion(request, completion: { reply, result in
                    print(result)
                    self.activityIndicator.stopAnimating()
                    if let button = sender as? UIButton {
                        button.isHidden = false
                    }
                    if reply != nil {
                        self.recommendationView.isHidden = false
                        self.recommendationName.text = reply!.name
                    }
                } )
        } catch (let error){
            print(error)
            self.recommendationView.isHidden = false
            self.recommendationName.text = error.localizedDescription
            self.activityIndicator.stopAnimating()
            if let button = sender as? UIButton {
                button.isHidden = false
            }
        }
    }
    @IBOutlet weak var activityIndicator: UIActivityIndicatorView!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        recommendationView.isHidden = true
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
