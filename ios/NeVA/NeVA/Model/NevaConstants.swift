//
//  NevaConstants.swift
//  NeVA
//
//  Created by Bilal Yaylak on 4.12.2017.
//  Copyright © 2017 Bilemiyorum Altan. All rights reserved.
//

import Foundation
import gRPC

class NevaConstants {
    static var service = Neva_Backend_BackendService.init(address: "neva.0xdeffbeef.com:50051", secure: true)
}
