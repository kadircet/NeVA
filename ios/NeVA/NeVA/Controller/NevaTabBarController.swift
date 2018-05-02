//
//  NevaTabBarController.swift
//  NeVA
//
//  Created by Bilal Yaylak on 2.01.2018.
//  Copyright © 2018 Bilemiyorum Altan. All rights reserved.
//

import UIKit

class NevaTabBarController: UITabBarController {

    override func viewDidLoad() {
        super.viewDidLoad()
        if #available(iOS 10.0, *) {
            //self.tabBar.tintColor = NeVAUtils.hexStringToUIColor(hex: "#BF360C")
            self.tabBar.unselectedItemTintColor = NeVAUtils.hexStringToUIColor(hex: "#FFCCBC")
        }
        NeVAUtils.getTagsFromServer()
        NeVAUtils.getMealsFromServer()
        NeVAUtils.fetchEntriesFromServer()
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
