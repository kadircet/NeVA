//
//  GenericButton.swift
//  NeVA
//
//  Created by Bilal Yaylak on 29.11.2017.
//  Copyright © 2017 Bilemiyorum Altan. All rights reserved.
//

import UIKit
@IBDesignable
class GenericButton: UIButton {

    @IBInspectable var textColor: UIColor? {
        didSet {
            setTitleColor(textColor, for: .normal)
            setTitleColor(UIColor.lightGray, for:.highlighted)
        }
    }
    @IBInspectable var bgColor: UIColor = UIColor.white {
        didSet {
            layer.backgroundColor = bgColor.cgColor
        }
    }
    @IBInspectable var isRounded: Bool = true
    
    // Only override draw() if you perform custom drawing.
    // An empty implementation adversely affects performance during animation.
    override func draw(_ rect: CGRect) {
        // Drawing code
        if(isRounded) {
            layer.cornerRadius = rect.height/2
        }
    }

}
