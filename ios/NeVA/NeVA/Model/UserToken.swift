//
//  UserToken.swift
//  NeVA
//
//  Created by Bilal Yaylak on 28.11.2017.
//  Copyright © 2017 Bilemiyorum Altan. All rights reserved.
//

import Foundation

class UserToken {
    static var token: Data?
    static var email: String?
    static var type: AuthenticationType?
    
    enum AuthenticationType
    {
        case default_type
        case facebook
    }
    //Other information about user may be contained in this class in the future
}
