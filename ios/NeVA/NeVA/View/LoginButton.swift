//
//  LoginButton.swift
//  NeVA
//
//  Created by Bilal Yaylak on 6.11.2017.
//  Copyright © 2017 Bilemiyorum Altan. All rights reserved.
//

import UIKit
@IBDesignable
class LoginButton: UIButton {

    override init(frame: CGRect) {
        super.init(frame: frame)
        setTitle("LOGIN", for: .normal)
        setTitleColor(UIColor.white, for: .normal)
        layer.backgroundColor = UIColor(red: 104.0/255.0, green: 159.0/255.0, blue: 56.0/255.0, alpha: 1.0).cgColor
    }
    
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
        setTitle("LOGIN", for: .normal)
        setTitleColor(UIColor.white, for: .normal)
        layer.backgroundColor = UIColor(red: 104.0/255.0, green: 159.0/255.0, blue: 56.0/255.0, alpha: 1.0).cgColor
    }
    // Only override draw() if you perform custom drawing.
    // An empty implementation adversely affects performance during animation.
    override func draw(_ rect: CGRect) {
        // Drawing code
        layer.cornerRadius = rect.height/2
    }
    

}
