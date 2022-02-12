//
//  DeleteViewController.swift
//  TwitterSeller
//
//  Created by Apps4SI Club on 2/3/22.
//

import UIKit
import SQLite3

class DeleteViewController: UIViewController {
    @IBOutlet var listingTableView: UITableView!
    @IBOutlet var cancelButton: UIButton!
    @IBOutlet var editButton: UIButton!
    @IBOutlet var deleteButton: UIButton!
    
    var mainViewController: MainViewController!
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
    var selectedRowNum = -1
    var curListingsInStrings:[String] = []
    var curActiveListingId : String = ""
    var jsonString: String!
    var header:String!
    var end:String!

    @IBAction func cancelButtonClicked() {
        dismiss(animated: true, completion: nil)
    }
    @IBAction func editButtonClicked() {
        editListing()
    }
    
    @IBAction func deleteButtonClicked() {
        if curActiveListingId.count > 1  {
            deleteListing( id:curActiveListingId)
        }
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        listingTableView.isScrollEnabled = true
        listingTableView.delegate = self
        listingTableView.dataSource = self
        editButton.isHidden = true
        deleteButton.isHidden = true
        populateListingTable();
        listingTableView.reloadData()
    }
    
    func populateListingTable() {
        curListingsInStrings = []
        let query = "SELECT * FROM " + TABLE_LISTINGS + ";"
        var statement : OpaquePointer? = nil
        if sqlite3_prepare_v2(mainViewController.db, query, -1, &statement, nil) == SQLITE_OK {
            while sqlite3_step(statement) == SQLITE_ROW {
                let productID = String(describing: String(cString: sqlite3_column_text(statement, 0)))
                let productName = String(describing: String(cString: sqlite3_column_text(statement, 1)))
                let productPrice = String(describing: String(cString: sqlite3_column_text(statement, 6)))
                let postDate = String(describing: String(cString: sqlite3_column_text(statement, 10)))
                let theListing = postDate + " " + productName + " for $" + productPrice + "  ID:" + productID;
                curListingsInStrings.append(theListing)
            }
        }
        sqlite3_finalize(statement)
    }
    
    func deleteListing(id: String) {
        
        let toSend: NSMutableDictionary = NSMutableDictionary()
        
        toSend.setValue("delete", forKey: "type")
        toSend.setValue(curActiveListingId, forKey: "id")
        var totalLength = 10
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
            DispatchQueue.global(qos: .userInitiated).async {
                self.deleteListingRemotely()
                
                // Perform all UI updates on the main queue
                DispatchQueue.main.async {
                    //Remote deleting is done.
                    self.deleteListingLocally(id: self.curActiveListingId)
                    self.populateListingTable()
                    self.listingTableView.reloadData()
                }
            }
        } catch {
            print("JSON serialization failed: ", error)
        }
    }
    
    func deleteListingRemotely() {
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
        print("I am here 0", writeLength)
        inputStream.close()
        outputStream.close()
    }
    
    func deleteListingLocally(id:String) {
        let query = "DELETE FROM " + TABLE_LISTINGS + " WHERE id = " + id + ";"
        if  sqlite3_exec(mainViewController.db, query, nil, nil, nil ) != SQLITE_OK {
            print("Failed to create list")
        }
    }
    
    func editListing() {
        DispatchQueue.main.async {
            //Remote deleting is done.
            self.mainViewController.loadFromDatabase(id: self.curActiveListingId);
            self.dismiss(animated: true, completion:nil)
        }
    }
}

extension DeleteViewController: UITableViewDelegate {
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        let listingText = curListingsInStrings[indexPath.row]
        var id:String = ""
        var subString:String = ""
        var n = 0
        n = listingText.count
        for i in 1...(n-1) {
            id = subString
            subString = String(listingText.suffix(i))
            if ( subString.contains(":")) {
                break
            }
        }
        //Remember the active Id
        curActiveListingId = id
        editButton.isHidden = false
        deleteButton.isHidden = false
    }
}


extension DeleteViewController: UITableViewDataSource {
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return curListingsInStrings.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "listing", for:indexPath )
        cell.textLabel?.text = curListingsInStrings[indexPath.row]
        return cell
    }
}
