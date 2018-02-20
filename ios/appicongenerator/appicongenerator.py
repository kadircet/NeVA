import os

icon_list = { 'iphone_notification@2x': '40x40',
        'iphone_notification@3x': '60x60',
        'iphone_spotlight_settings@2x': '58x58',
        'iphone_spotlight_settings@3x': '87x87',
        'iphone_spotlight@2x': '80x80',
        'iphone_spotlight@3x': '120x120',
        'iphone_app@2x': '120x120',
        'iphone_app@3x': '180x180',
        'ipad_notifications@1x': '20x20',
        'ipad_notifications@2x': '40x40',
        'ipad_settings@1x': '29x29',
        'ipad_settings@2x': '58x58',
        'ipad_spotlight@1x': '40x40',
        'ipad_spotlight@2x': '80x80',
        'ipad_app@1x': '76x76',
        'ipad_app@2x': '152x152',
        'ipad_pro_app@2x': '167x167',
        'app_store@1x': '1024x1024'
        }

for key in icon_list:
    os.system('cp logo_neva.png ./neva_ios_logos/logo_neva_' + key + '.png')
    os.system('(cd ./neva_ios_logos && mogrify -resize ' + icon_list[key] + ' logo_neva_' + key + '.png)')
