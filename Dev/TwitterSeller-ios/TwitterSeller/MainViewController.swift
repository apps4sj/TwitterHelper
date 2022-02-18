//
//  ViewController.swift
//  TwitterSeller
//
//  Created by Apps4SI Club on 1/23/22.
//

import UIKit
import SQLite3

import Foundation
import Network

//A singleton network monitor
final class NetworkMonitor {
    static let shared = NetworkMonitor()
    private let queue = DispatchQueue.global()
    private let monitor: NWPathMonitor
    public private(set) var isConnected: Bool = false
    
    private init() {
        monitor = NWPathMonitor()
    }
    
    public func startMonitoring() {
        monitor.start(queue: queue)
        monitor.pathUpdateHandler = { [weak self] path in
            self?.isConnected = (path.status == .satisfied)
        }
    }
    
    public func stopMonitoring() {
        monitor.cancel()
    }
}

class MainViewController: UIViewController {
    @IBOutlet var productInput:UITextField!
    @IBOutlet var descInput:UITextField!
    @IBOutlet var priceInput:UITextField!
    @IBOutlet var emailInput:UITextField!
    @IBOutlet var phoneNumInput:UITextField!
    @IBOutlet var locationInput:UITextField!
    @IBOutlet var sendButton:UIButton!
    @IBOutlet var myListingsButton:UIButton!
    @IBOutlet var imagePreview0: UIButton!
    @IBOutlet var imagePreview1: UIButton!
    @IBOutlet var imagePreview2: UIButton!
    var currentButton: UIButton!
    
    var imageFileName0: String!
    var imageFileName1: String!
    var imageFileName2: String!
    var jpegImage0 : Data?
    var jpegImage1 : Data?
    var jpegImage2 : Data?
    
    var jsonString: String!
    var header:String!
    var end:String!
    let buffer = UnsafeMutablePointer<UInt8>.allocate(capacity: 16*1024*1024)
    var bufferPointer : UnsafeMutableBufferPointer<UInt8>?
    var listId : String?
    // DataBase related
    var db: OpaquePointer?
    let DATABASE_NAME = "TwitterSeller"
    let TABLE_LISTINGS = "listings"
    let KEY_ID = "id"
    let KEY_PROD_NAME = "product_name"
    let KEY_PROD_DESC = "product_description"
    let KEY_EMAIL = "email"
    let KEY_LOCATION = "location"
    let KEY_PHONE = "phone"
    let KEY_PROD_PRICE = "price"
    let KEY_IMAGE1 = "image1"
    let KEY_IMAGE2 = "image2"
    let KEY_IMAGE3 = "image3"
    let KEY_DATE_POST = "date_posted"
    
    
    
    @IBAction func takeImage0() {
        takeImage(imagePreview: imagePreview0)
    }
    @IBAction func takeImage1() {
        takeImage(imagePreview: imagePreview1)
    }
    @IBAction func takeImage2() {
        takeImage(imagePreview: imagePreview2)
    }
    
    /// When the Send button is clicked
    @IBAction func sendButtonClicked() {
        //Need to check if network connection is available.
        if !NetworkMonitor.shared.isConnected {
            let alert = UIAlertController(title: "No Internet Connection!", message: "Please check your network connection!", preferredStyle: UIAlertController.Style.alert)
            alert.addAction(UIAlertAction(title: "Click", style: UIAlertAction.Style.default, handler: nil))
            self.present(alert, animated: true, completion: nil)
            return
        }
        
        let toSend: NSMutableDictionary = NSMutableDictionary()
        
        toSend.setValue("stage", forKey: "type")
        let n = Int.random(in: 100000000...999999999)
        let id = String(n)
        listId = id
        toSend.setValue(id, forKey: "id")
        toSend.setValue(productInput.text, forKey: "itemName")
        if (priceInput.text == nil ) {
            priceInput.insertText("0")
        }
        toSend.setValue(Float(priceInput.text!), forKey: "price")
        toSend.setValue(descInput.text, forKey: "description")
        toSend.setValue(locationInput.text, forKey: "location")
        
        let contact: NSMutableDictionary = NSMutableDictionary()
        contact.setValue(emailInput.text, forKey: "email")
        contact.setValue(phoneNumInput.text, forKey: "phoneNum")
        
        toSend.setValue(contact, forKey: "contact")
        var totalLength = 10
        
        if ( imageFileName0 != nil ) {
            let fileUrl = try!
                FileManager.default.url(for: .documentDirectory,
                                        in: .userDomainMask, appropriateFor: nil,
                                        create:false).appendingPathComponent(imageFileName0)
            if FileManager.default.fileExists(atPath: fileUrl.path) {
                do {
                    jpegImage0 = try Data(contentsOf: fileUrl)
                    if jpegImage0 != nil {
                        let imageJson: NSMutableDictionary = NSMutableDictionary()
                        imageJson.setValue("image0.jpg", forKey: "fileName")
                        imageJson.setValue(jpegImage0!.count, forKey: "length")
                        totalLength += jpegImage0!.count
                        toSend.setValue(imageJson, forKey: "image0")
                    }
                } catch {
                    print("Failed to load " + imageFileName0, error)
                }
            }
        }
        if ( imageFileName1 != nil ) {
            let fileUrl = try!
                FileManager.default.url(for: .documentDirectory,
                                        in: .userDomainMask, appropriateFor: nil,
                                        create:false).appendingPathComponent(imageFileName1)
            if FileManager.default.fileExists(atPath: fileUrl.path) {
                do {
                    jpegImage1 = try Data(contentsOf: fileUrl)
                    if jpegImage1 != nil {
                        let imageJson: NSMutableDictionary = NSMutableDictionary()
                        imageJson.setValue("image1.jpg", forKey: "fileName")
                        imageJson.setValue(jpegImage1!.count, forKey: "length")
                        totalLength += jpegImage1!.count
                        toSend.setValue(imageJson, forKey: "image1")
                    }
                } catch {
                    print("Failed to load " + imageFileName1, error)
                }
            }
        }
        if ( imageFileName2 != nil ) {
            let fileUrl = try!
                FileManager.default.url(for: .documentDirectory,
                                        in: .userDomainMask, appropriateFor: nil,
                                        create:false).appendingPathComponent(imageFileName2)
            if FileManager.default.fileExists(atPath: fileUrl.path) {
                do {
                    jpegImage2 = try Data(contentsOf: fileUrl)
                    if jpegImage2 != nil {
                        let imageJson: NSMutableDictionary = NSMutableDictionary()
                        imageJson.setValue("image2.jpg", forKey: "fileName")
                        imageJson.setValue(jpegImage2!.count, forKey: "length")
                        totalLength += jpegImage2!.count
                        toSend.setValue(imageJson, forKey: "image2")
                    }
                } catch {
                    print("Failed to load " + imageFileName2, error)
                }
            }
        }
        
        do {
            let data = try JSONSerialization.data(withJSONObject: toSend)
            jsonString = String(data: data, encoding: .utf8)!
            let jsonLength = jsonString.lengthOfBytes(using: .utf8)
            totalLength += jsonLength
            
            header = String(totalLength)
            while header.lengthOfBytes(using: .utf8) < 9 {
                header = "0" + header
            }
            header += "\n"
            end = "\n"
            self.showSpinner(onView: self.view)
            DispatchQueue.global(qos: .userInitiated).async {
                self.performStaging()
                
                // Perform all UI updates on the main queue
                DispatchQueue.main.async {
                    //Staging is done
                    self.removeSpinner()
                    let previewVC = self.storyboard?.instantiateViewController(withIdentifier: "preview_VC") as! PreviewViewController
                    previewVC.mainViewController = self
                    previewVC.jpegBufferPointer = self.bufferPointer
                    previewVC.listId = self.listId
                    self.present(previewVC, animated: true, completion: nil)
                }
            }
        } catch {
            print("JSON serialization failed: ", error)
        }
    }
    
    /// When My Listings Button is clicked
    @IBAction func myListingsButtonClicked() {
        let deleteVC = self.storyboard?.instantiateViewController(withIdentifier: "delete_VC") as! DeleteViewController
        deleteVC.mainViewController = self
        self.present(deleteVC, animated: true, completion: nil)
    }


    ///
    /// The viewDidLoad() func
    ///
    override func viewDidLoad() {
        super.viewDidLoad()
        DispatchQueue.global(qos: .userInitiated).async {
            self.controlNumberOfJpegFiles()
        }
        
        // Do any additional setup after loading the view.
        let fileUrl = try!
            FileManager.default.url(for: .documentDirectory,
                                    in: .userDomainMask, appropriateFor: nil,
                                    create:false).appendingPathComponent( DATABASE_NAME + ".sqlite")
        
        if sqlite3_open(fileUrl.path, &db ) != SQLITE_OK {
            print("ERROR failed to open database")
            dismiss(animated: true, completion: nil)
        }
        
        let createTableQuery = "CREATE TABLE IF NOT EXISTS " + TABLE_LISTINGS + " ("
            + KEY_ID + " TEXT PRIMARY KEY," + KEY_PROD_NAME + " TEXT,"
            + KEY_PROD_DESC + " TEXT," + KEY_EMAIL + " TEXT," + KEY_LOCATION
            + " TEXT," + KEY_PHONE + " TEXT," + KEY_PROD_PRICE + " TEXT," +
            KEY_IMAGE1 + " TEXT," + KEY_IMAGE2 + " TEXT," + KEY_IMAGE3 + " TEXT," +
            KEY_DATE_POST + " TEXT" + ");"
        
        if  sqlite3_exec(db, createTableQuery, nil, nil, nil ) != SQLITE_OK {
            print("Failed to create list")
        }
        print("Everything is fine")
    }
    
    func takeImage(imagePreview: UIButton) {
        currentButton = imagePreview
        let picker = UIImagePickerController()
        picker.sourceType = .camera
        picker.delegate = self
        present(picker, animated:true)
    }
    
    // sending stageing data blob over the network and receive the jpeg preview
    // Note: runs on the network thread
    func performStaging() {
        var readStream: Unmanaged<CFReadStream>?
        var writeStream: Unmanaged<CFWriteStream>?
        CFStreamCreatePairWithSocketToHost(kCFAllocatorDefault,
                                           "apps4sj.org" as CFString,
                                           32421,
                                           &readStream,
                                           &writeStream)
        let inputStream: InputStream! = readStream!.takeRetainedValue()
        let outputStream: OutputStream! = writeStream!.takeRetainedValue()
        inputStream.open()
        outputStream.open()
        var encodedDataArray = [UInt8](header.utf8)
        var writeLength = outputStream.write(encodedDataArray, maxLength: encodedDataArray.count)
        encodedDataArray = [UInt8](jsonString.utf8)
        writeLength += outputStream.write(encodedDataArray, maxLength: encodedDataArray.count)
        encodedDataArray = [UInt8](end.utf8)
        writeLength += outputStream.write(encodedDataArray, maxLength: encodedDataArray.count)
        
        if jpegImage0 != nil {
            writeLength += outputStream.write(jpegImage0!)
        }
        if jpegImage1 != nil {
            writeLength += outputStream.write(jpegImage1!)
        }
        if jpegImage2 != nil {
            writeLength += outputStream.write(jpegImage2!)
        }
        
        var read = inputStream.read(buffer, maxLength: 10)
        for _ in 0...15 {
            if  read <= 0 {
                Thread.sleep(forTimeInterval: 0.3)
                read = inputStream.read(buffer, maxLength: 10)
            } else {
                break
            }
        }
        var totalRead = 0
        while ( read > 0 ) {
            read = inputStream.read(buffer + totalRead, maxLength: 1024)
            for _ in 0...15 {
                if  read <= 0 {
                    print("failed to read!!!!!")
                    Thread.sleep(forTimeInterval: 0.3)
                    read = inputStream.read(buffer, maxLength: 1024)
                } else {
                    break
                }
            }
            totalRead += read
        }
        bufferPointer = UnsafeMutableBufferPointer(start: buffer, count: totalRead)
        inputStream.close()
        outputStream.close()
    }
    
    func saveToDatabase() {
        
        let query = "INSERT INTO " + TABLE_LISTINGS + " (" + KEY_ID + ","
            + KEY_PROD_NAME + "," + KEY_PROD_DESC + "," + KEY_EMAIL + ","
            + KEY_LOCATION + "," + KEY_PHONE + "," + KEY_PROD_PRICE + ","
            + KEY_IMAGE1 + "," + KEY_IMAGE2 + "," + KEY_IMAGE3 + ","
            + KEY_DATE_POST + " ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? );"
        
        var statement : OpaquePointer? = nil
        if sqlite3_prepare_v2(db, query, -1, &statement, nil) == SQLITE_OK{
            let id = listId!
            let productName = productInput.text!
            let productDesc = descInput.text!
            let emailAddress = emailInput.text!
            let locationAddress = locationInput.text!
            let phoneNumber = phoneNumInput.text!
            let productPrice = priceInput.text!
            var image0Address = "x"
            var image1Address = "x"
            var image2Address = "x"
            if  imageFileName0 != nil {
                image0Address = imageFileName0
            }
            if  imageFileName1 != nil {
                image1Address = imageFileName1
            }
            if  imageFileName2 != nil {
                image2Address = imageFileName2
            }
            
            let dateFormatter = DateFormatter()
            dateFormatter.dateStyle = .short
            dateFormatter.timeStyle = .short
            dateFormatter.locale = Locale(identifier: "en_US")
            let date = Date(timeIntervalSinceReferenceDate: 118800)
            let postDate = dateFormatter.string(from: date)
            //let postDate = "This_is_wrong"
            print(id)
            print(query)
            print(postDate)
            
            sqlite3_bind_text(statement, 1, (id as NSString).utf8String, -1, nil)
            sqlite3_bind_text(statement, 2, (productName as NSString).utf8String, -1, nil)
            sqlite3_bind_text(statement, 3, (productDesc as NSString).utf8String, -1, nil)
            sqlite3_bind_text(statement, 4, (emailAddress as NSString).utf8String, -1, nil)
            sqlite3_bind_text(statement, 5, (locationAddress as NSString).utf8String, -1, nil)
            sqlite3_bind_text(statement, 6, (phoneNumber as NSString).utf8String, -1, nil)
            sqlite3_bind_text(statement, 7, (productPrice as NSString).utf8String, -1, nil)
            sqlite3_bind_text(statement, 8, (image0Address as NSString).utf8String, -1, nil)
            sqlite3_bind_text(statement, 9, (image1Address as NSString).utf8String, -1, nil)
            sqlite3_bind_text(statement, 10, (image2Address as NSString).utf8String, -1, nil)
            sqlite3_bind_text(statement, 11, (postDate as NSString).utf8String, -1, nil)
            
            let ex_string = String(cString: sqlite3_expanded_sql(statement))
            print(ex_string)
            let rc = sqlite3_step(statement)
            
            if rc == SQLITE_DONE {
                print("Data inserted success")
            }else {
                print("Error code is ", rc, String(cString:sqlite3_errmsg(db!)))
                print("Data is not inserted in table")
            }
        } else {
            print("Query is not as per requirement")
        }
        sqlite3_finalize(statement)
    }
    
    func loadFromDatabase(id:String!) {
        
        let query = "SELECT * FROM " + TABLE_LISTINGS + " WHERE " + KEY_ID + " = " + id + ";"
        var statement : OpaquePointer? = nil
        if sqlite3_prepare_v2(db, query, -1, &statement, nil) == SQLITE_OK {
            while sqlite3_step(statement) == SQLITE_ROW {
                let productName = String(describing: String(cString: sqlite3_column_text(statement, 1)))
                let productDesc = String(describing: String(cString: sqlite3_column_text(statement, 2)))
                let emailAddress = String(describing: String(cString: sqlite3_column_text(statement, 3)))
                let locationAddress = String(describing: String(cString: sqlite3_column_text(statement, 4)))
                let phoneNumber = String(describing: String(cString: sqlite3_column_text(statement, 5)))
                let productPrice = String(describing: String(cString: sqlite3_column_text(statement, 6)))
                let image0Name = String(describing: String(cString: sqlite3_column_text(statement, 7)))
                let image1Name = String(describing: String(cString: sqlite3_column_text(statement, 8)))
                let image2Name = String(describing: String(cString: sqlite3_column_text(statement, 9)))
                
                productInput.text = productName
                descInput.text = productDesc
                locationInput.text = locationAddress
                priceInput.text = productPrice
                emailInput.text = emailAddress
                phoneNumInput.text = phoneNumber
                imageFileName0 = nil
                if image0Name.count > 2 {
                    let fileUrl = try!
                        FileManager.default.url(for: .documentDirectory,
                                                in: .userDomainMask, appropriateFor: nil,
                                                create:false).appendingPathComponent(image0Name)
                    if FileManager.default.fileExists(atPath: fileUrl.path) {
                        let recreatedImage = UIImage(contentsOfFile: fileUrl.path)
                        imagePreview0.setImage(recreatedImage, for: .normal)
                        imageFileName0 = image0Name
                    }
                }
                imageFileName1 = nil
                if image1Name.count > 2 {
                    let fileUrl = try!
                        FileManager.default.url(for: .documentDirectory,
                                                in: .userDomainMask, appropriateFor: nil,
                                                create:false).appendingPathComponent(image1Name)
                    if FileManager.default.fileExists(atPath: fileUrl.path) {
                        let recreatedImage = UIImage(contentsOfFile: fileUrl.path)
                        imagePreview1.setImage(recreatedImage, for: .normal)
                        imageFileName1 = image1Name
                    }
                }
                imageFileName2 = nil
                if image2Name.count > 2 {
                    let fileUrl = try!
                        FileManager.default.url(for: .documentDirectory,
                                                in: .userDomainMask, appropriateFor: nil,
                                                create:false).appendingPathComponent(image2Name)
                    if FileManager.default.fileExists(atPath: fileUrl.path) {
                        let recreatedImage = UIImage(contentsOfFile: fileUrl.path)
                        imagePreview2.setImage(recreatedImage, for: .normal)
                        imageFileName2 = image0Name
                    }
                }
            }
        }
        sqlite3_finalize(statement)
    }
    
    func controlNumberOfJpegFiles() {
        let paths = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)[0]
        guard let directoryURL = URL(string: paths.path) else {return}
        do {
            let allFiles = try
                FileManager.default.contentsOfDirectory(at: directoryURL,
                                                        includingPropertiesForKeys:[.contentModificationDateKey],
                                                        options: [.skipsHiddenFiles, .skipsSubdirectoryDescendants])
                .filter { $0.lastPathComponent.hasSuffix(".jpg") }
                .sorted(by: {
                    let date0 = try $0.promisedItemResourceValues(forKeys:[.contentModificationDateKey]).contentModificationDate!
                    let date1 = try $1.promisedItemResourceValues(forKeys:[.contentModificationDateKey]).contentModificationDate!
                    return date0.compare(date1) == .orderedDescending
                })
            
            //Delete the oldest files to make sure total file number is less than 100
            let maxFileNum = 100
            if allFiles.count > maxFileNum {
                let filesToDelete = allFiles[maxFileNum...(allFiles.count-1)]
                for item in filesToDelete {
                    do {
                        try FileManager.default.removeItem(at:item.absoluteURL)
                    } catch let error as NSError {
                        print("Error: \(error.domain)")
                    }
                }
            }
        } catch {
            print (error)
        }
    }
}

/////////////////////////////////////////////////////////////////
/// Extend MainViewController for taking pictures
/////////////////////////////////////////////////////////////////
extension MainViewController: UIImagePickerControllerDelegate,
                              UINavigationControllerDelegate {
    func imagePickerControllerDidCancel(_ picker: UIImagePickerController) {
        picker.dismiss(animated: true, completion: nil)
    }
    
    func imagePickerController(_ picker: UIImagePickerController,
                               didFinishPickingMediaWithInfo info:[UIImagePickerController.InfoKey: Any]) {
        picker.dismiss(animated: true, completion: nil)
        guard let image=info[UIImagePickerController.InfoKey.originalImage] as?
                UIImage else {
            return
        }
        let imageJpegData = image.jpegData(compressionQuality: 0.70)
        let n = Int.random(in: 100000000...999999999)
        let imageFileName = String(n) + ".jpg"
        let fileUrl = try!
            FileManager.default.url(for: .documentDirectory,
                                    in: .userDomainMask, appropriateFor: nil,
                                    create:false).appendingPathComponent(imageFileName)
        if !FileManager.default.fileExists(atPath: fileUrl.path) {
            do {
                // writes the image data to disk
                try imageJpegData!.write(to: fileUrl)
                let recreatedImage = UIImage(contentsOfFile: fileUrl.path)
                currentButton.setImage(recreatedImage, for: .normal)
                if ( currentButton == imagePreview0 ) {
                    imageFileName0 = imageFileName;
                }
                if ( currentButton == imagePreview1 ) {
                    imageFileName1 = imageFileName;
                }
                if ( currentButton == imagePreview2 ) {
                    imageFileName2 = imageFileName;
                }
                print("Image file saved")
            } catch {
                print("error saving image file:", error)
            }
        }
    }
}

/////////////////////////////////////////////////////////////////
///To display a Loading/Waiting Activity Indicator
///Copied from https://brainwashinc.com/2017/07/21/loading-activity-indicator-ios-swift/
/////////////////////////////////////////////////////////////////
var vSpinner : UIView?
extension UIViewController {
    func showSpinner(onView : UIView) {
        let spinnerView = UIView.init(frame: onView.bounds)
        spinnerView.backgroundColor = UIColor.init(red: 0.5, green: 0.5, blue: 0.5, alpha: 0.5)
        let ai = UIActivityIndicatorView.init(style: .whiteLarge)
        ai.startAnimating()
        ai.center = spinnerView.center
        
        DispatchQueue.main.async {
            spinnerView.addSubview(ai)
            onView.addSubview(spinnerView)
        }
        
        vSpinner = spinnerView
    }
    
    func removeSpinner() {
        DispatchQueue.main.async {
            vSpinner?.removeFromSuperview()
            vSpinner = nil
        }
    }
}

/////////////////////////////////////////////////////////////////
/// Extend OutputStream for sending pictures
/////////////////////////////////////////////////////////////////
extension OutputStream {
    func write(_ data: Data) -> Int {
        data.withUnsafeBytes { dataPointer in
            var tempBuffer = dataPointer.bindMemory(to: UInt8.self)
            while !tempBuffer.isEmpty {
                let theMaxLength = min(1024, tempBuffer.count)
                let written = self.write(tempBuffer.baseAddress!, maxLength: theMaxLength)
                guard written >= 0 else {
                    return -1
                }
                tempBuffer = .init(rebasing: tempBuffer.dropFirst(written))
            }
            return dataPointer.count
        }
    }
}
