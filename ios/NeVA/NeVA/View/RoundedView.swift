//
//  RoundedView.swift
//  NeVA
//
//  Created by Bilal Yaylak on 20.11.2017.
//  Copyright © 2017 Bilemiyorum Altan. All rights reserved.
//

import UIKit

@IBDesignable
class RoundedView: UIView {

    override func draw(_ rect: CGRect) {
        // Drawing code
        layer.cornerRadius = rect.height/2
        layer.borderColor = UIColor.white.cgColor
        layer.borderWidth = 1.0
        layer.masksToBounds = true
    }

}
