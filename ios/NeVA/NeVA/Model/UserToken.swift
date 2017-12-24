//
//  UserToken.swift
//  NeVA
//
//  Created by Bilal Yaylak on 28.11.2017.
//  Copyright © 2017 Bilemiyorum Altan. All rights reserved.
//

import Foundation
import KeychainSwift

class UserToken {
    static var keychain = KeychainSwift(keyPrefix: "NeVA")
    
    static var token: Data? = nil
    static var email: String? = nil
    static var type: AuthenticationType? = nil
    static func setUserToken(email: String, token: Data) {
        UserToken.email = email
        UserToken.token = token
        UserToken.type = .default_type
        keychain.set(email, forKey: "email")
        keychain.set(token, forKey: "token")
    }
    static func storeUserToken() {
        if token != nil, email != nil {
            keychain.set(email!, forKey: "email")
            keychain.set(token!, forKey: "token")
        }
    }
    static func clearUserToken() {
        UserToken.email = nil
        UserToken.token = nil
        UserToken.type = nil
        keychain.delete("email")
        keychain.delete("token")
    }
    static func initializeToken() -> Bool {
        let emailWillBeUsed = keychain.get("email")
        let tokenWillBeUsed = keychain.getData("token")
        if emailWillBeUsed != nil {
            if tokenWillBeUsed != nil {
                UserToken.email = emailWillBeUsed
                UserToken.token = tokenWillBeUsed
                UserToken.type = .default_type
                return true
            } else {
                keychain.delete("email")
                UserToken.email = nil
                UserToken.token = nil
                UserToken.type = nil
            }
        } else {
            if tokenWillBeUsed != nil {
               keychain.delete("token")
            }
            UserToken.email = nil
            UserToken.token = nil
            UserToken.type = nil
        }
        return false
    }
    
    enum AuthenticationType
    {
        case default_type
        case facebook
    }
    //Other information about user may be contained in this class in the future
}
