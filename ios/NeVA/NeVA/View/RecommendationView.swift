//
//  RecommendationView.swift
//  NeVA
//
//  Created by Bilal Yaylak on 2.01.2018.
//  Copyright © 2018 Bilemiyorum Altan. All rights reserved.
//

import UIKit

class RecommendationView: UIView {

    @IBOutlet var contentView: UIView!
    @IBOutlet weak var name: UILabel!
    @IBOutlet weak var image: UIImageView!
    override init(frame: CGRect) {
        super.init(frame: frame)
        xibinit()
    }
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
        xibinit()
    }
    
    func xibinit() {
        Bundle.main.loadNibNamed("RecommendationView", owner: self, options: nil)
        addSubview(contentView)
        contentView.frame = self.bounds
        contentView.autoresizingMask = [.flexibleHeight, .flexibleWidth]
        //TODO: It seems good because xib and view in mainboard have same size find some way to do it better
        //image.layer.cornerRadius = image.frame.height / 2
        //image.layer.borderColor = NeVAColors.primaryDarkColor.cgColor
        //image.layer.borderWidth = 3.0
        contentView.layer.borderColor = NeVAColors.primaryColor.cgColor
        contentView.layer.cornerRadius = image.frame.height / 5
        contentView.layer.borderWidth = 4.0
    }
    /*
    // Only override draw() if you perform custom drawing.
    // An empty implementation adversely affects performance during animation.
    override func draw(_ rect: CGRect) {
        // Drawing code
    }
    */

}
