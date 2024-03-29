//
//  LoginRegisterInputField.swift
//  NeVA
//
//  Created by Bilal Yaylak on 7.11.2017.
//  Copyright © 2017 Bilemiyorum Altan. All rights reserved.
//

import UIKit
@IBDesignable
class CustomInputField: UITextField {

    @IBInspectable var placeholderText: String? = "" {
        didSet {
            placeholder = placeholderText
        }
    }
    @IBInspectable var placeholderColor: UIColor? = UIColor.white {
        didSet {
            attributedPlaceholder=NSAttributedString(string: placeholder ?? "" , attributes: [NSAttributedStringKey.foregroundColor: placeholderColor!])
        }
    }
    override init(frame: CGRect) {
        super.init(frame: frame)
        layer.backgroundColor = UIColor.clear.cgColor
        layer.borderColor = NeVAColors.primaryColor.cgColor
        //layer.borderColor = UIColor.white.cgColor
        layer.borderWidth = 1.0
    }
    
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
        layer.backgroundColor = UIColor.clear.cgColor
        layer.borderColor = NeVAColors.primaryColor.cgColor
        //layer.borderColor = UIColor.white.cgColor
        layer.borderWidth = 1.0
    }
    // Only override draw() if you perform custom drawing.
    // An empty implementation adversely affects performance during animation.
    override func draw(_ rect: CGRect) {
        // Drawing code
        layer.cornerRadius = rect.height/2
    }
    
    
}

extension UIView {
    func shake() {
        let animation = CAKeyframeAnimation(keyPath: "transform.translation.x")
        animation.timingFunction = CAMediaTimingFunction(name: kCAMediaTimingFunctionLinear)
        animation.duration = 0.6
        animation.values = [-20.0, 20.0, -20.0, 20.0, -10.0, 10.0, -5.0, 5.0, 0.0 ]
        layer.add(animation, forKey: "shake")
    }
}
