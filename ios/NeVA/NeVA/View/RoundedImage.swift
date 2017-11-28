//
//  RoundedImage.swift
//  NeVA
//
//  Created by Bilal Yaylak on 29.11.2017.
//  Copyright © 2017 Bilemiyorum Altan. All rights reserved.
//

import UIKit

@IBDesignable
class RoundedImage: UIImageView {
    
    @IBInspectable var borderColor: UIColor? {
        didSet {
            layer.borderColor = borderColor?.cgColor
        }
    }
    @IBInspectable var borderWidth: CGFloat = 0.0 {
        didSet {
            layer.borderWidth = borderWidth
        }
    }
    @IBInspectable var isRounded: Bool = false {
        didSet {
            if isRounded {
                layer.cornerRadius = frame.size.width / 2;
            } else {
                layer.cornerRadius = 0
            }
        }
    }
    // Only override draw() if you perform custom drawing.
    // An empty implementation adversely affects performance during animation.
    /*override func draw(_ rect: CGRect) {
        // Drawing code
        layer.cornerRadius = rect.height/2
    }
     */
 

}
