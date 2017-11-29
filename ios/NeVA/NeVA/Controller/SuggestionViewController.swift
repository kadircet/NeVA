//
//  SuggestionViewController.swift
//  NeVA
//
//  Created by Bilal Yaylak on 28.11.2017.
//  Copyright © 2017 Bilemiyorum Altan. All rights reserved.
//

import UIKit

class SuggestionViewController: UIViewController, UIPickerViewDataSource, UIPickerViewDelegate {
    func numberOfComponents(in pickerView: UIPickerView) -> Int {
       return 1
    }
    
    func pickerView(_ pickerView: UIPickerView, numberOfRowsInComponent component: Int) -> Int {
        if pickerView == tagPickerView {
            return tagList.count
        }
        else if pickerView == foodPickerView {
            return foodList.count
        }
        else {
            return 0
        }
    }
    
    func pickerView(_ pickerView: UIPickerView, titleForRow row: Int, forComponent component: Int) -> String? {
        if pickerView == tagPickerView {
            return tagList[row]
        }
        else if pickerView == foodPickerView {
            return foodList[row]
        }
        else {
            return ""
        }
    }
    func pickerView(_ pickerView: UIPickerView, didSelectRow row: Int, inComponent component: Int) {
        if pickerView == tagPickerView {
            tagPickerField.text = tagList[row]
        }
        else if pickerView == foodPickerView {
            foodPickerField.text = foodList[row]
        }
    }
    

    //Picker View Functions
    
    //
    @IBAction func suggestTagButtonPressed(_ sender: Any) {
        if let tag = tagSuggestionField.text, !tag.isEmpty {
            let service = Neva_Backend_BackendService.init(address: "0xdeffbeef.com:50051")
            var request = Neva_Backend_TagPropositionRequest()
            request.token = UserToken.token!
            request.tag = tag
            do {
                let responseMessage = try service.tagproposition(request)
                print(responseMessage)
                if let button = sender as? UIButton {
                    button.shake()
                }
                tagSuggestionField.text = ""
                tagSuggestionField.placeholder = "Successful"
            } catch (let error) {
                print(error)
                tagSuggestionField.text = ""
                tagSuggestionField.placeholder = "Failed"
                tagSuggestionField.shake()
            }
        }
        else {
            tagSuggestionField.shake()
        }
    }

    @IBAction func suggestFoodNameButtonPressed(_ sender: Any) {
        if let name = foodSuggestionField.text, !name.isEmpty {
            let service = Neva_Backend_BackendService.init(address: "0xdeffbeef.com:50051")
            var request = Neva_Backend_SuggestionItemPropositionRequest()
            request.suggestion = Neva_Backend_Suggestion()
            request.suggestion.name = name
            request.suggestion.suggestionCategory = .meal
            request.token = UserToken.token!
            do {
                let responseMessage = try service.suggestionitemproposition(request)
                print(responseMessage)
                if let button = sender as? UIButton {
                    button.shake()
                }
                foodSuggestionField.text = ""
                foodSuggestionField.placeholder = "Successful"
            } catch (let error) {
                print(error)
                foodSuggestionField.text = ""
                foodSuggestionField.placeholder = "Failed"
                foodSuggestionField.shake()
            }
        }
        else {
            foodSuggestionField.shake()
        }
    }
    
    @IBOutlet weak var tagSuggestionField: UITextField!
    
    @IBOutlet weak var foodSuggestionField: UITextField!
    
    @IBOutlet weak var tagPickerField: UITextField!
    @IBOutlet weak var foodPickerField: LoginRegisterInputField!
    
    let tagPickerView = UIPickerView()
    let foodPickerView = UIPickerView()
    
    var tagList = ["Fast Food", "Healthy", "Vegan"]
    var foodList = ["Lahmacun", "Brokoli Salatası", "Hamburger", "Kebap"]
    
    override func viewDidLoad() {
        super.viewDidLoad()
        tagPickerView.delegate = self
        tagPickerField.inputView = tagPickerView
        foodPickerView.delegate = self
        foodPickerField.inputView = foodPickerView
        let tap = UITapGestureRecognizer(target: self.view, action: #selector(UIView.endEditing(_:)))
        tap.cancelsTouchesInView = false
        self.view.addGestureRecognizer(tap)
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
