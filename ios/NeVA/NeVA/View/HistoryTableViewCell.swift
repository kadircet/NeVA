//
//  HistoryTableViewCell.swift
//  NeVA
//
//  Created by Bilal Yaylak on 4.12.2017.
//  Copyright © 2017 Bilemiyorum Altan. All rights reserved.
//

import UIKit

class HistoryTableViewCell: UITableViewCell {
    
    @IBOutlet weak var foodPicture: UIImageView!
    @IBOutlet weak var mealName: UILabel!
    @IBOutlet weak var time: UILabel!
    
    func setName(name: String) {
        mealName.text = name
    }
    func setFoodPicture(picture: UIImage) {
        foodPicture.image = picture
    }
    func setFoodPicture(name: String) {
        let color = UIColor.colorHash(name: name)
        let componentColors = color.cgColor.components
        let colorBrightness = ((componentColors![0] * 299) + (componentColors![1] * 587) + (componentColors![2] * 114)) / 1000
        var textColor: UIColor? = nil
        if (colorBrightness < 0.5)
        {
            //Background is dark
            textColor = UIColor.white
        }
        else
        {
            //Background is white
            textColor = UIColor.black
        }
        let font = UIFont.systemFont(ofSize: 50.0)
        let initials = name.initials
        let index = initials.index(after: initials.startIndex)
        let firstLetter = String(initials[..<index])
        foodPicture.setImage(string: firstLetter, color: color, circular: true, textAttributes: [NSAttributedStringKey.font: font, NSAttributedStringKey.foregroundColor: textColor!])
    }
    func setTime(time: Date) {
        let formatter = DateFormatter()
        formatter.timeZone = TimeZone(secondsFromGMT: 0)
        formatter.dateFormat = "HH:mm"
        self.time.text = formatter.string(from: time)
    }
    func setTime(hour: Int, min: Int) {
        var hourString: String = ""
        var minString: String = ""
        if hour >= 0, hour < 10 {
            hourString = "0\(hour)"
        } else {
            hourString = "\(hour)"
        }
        if min >= 0, min < 10 {
            minString = "0\(min)"
        } else {
            minString = "\(min)"
        }
        self.time.text = "\(hourString):\(minString)"
        
    }
    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
    }

    /*override func setSelected(_ selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)

        // Configure the view for the selected state
    }*/

}
