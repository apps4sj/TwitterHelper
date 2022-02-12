//
//  PreviewViewController.swift
//  TwitterSeller
//
//  Created by Apps4SI Club on 2/1/22.
//

import UIKit

class PreviewViewController: UIViewController {
    var mainViewController: MainViewController?
    var jpegBufferPointer : UnsafeMutableBufferPointer<UInt8>?
    var jsonString: String!
    var header:String!
    var end:String!

    var listId : String?
    @IBOutlet var webpagePreview: UIImageView!
    @IBOutlet var publishButton: UIButton!
    @IBOutlet var cancelButton: UIButton!

    @IBAction func publishButtonClicked() {
        let toSend: NSMutableDictionary = NSMutableDictionary()

        toSend.setValue("publish", forKey: "type")
        toSend.setValue(listId, forKey: "id")
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
              self.performPublishing()

              // Perform all UI updates on the main queue
              DispatchQueue.main.async {
                print("Publishing is done")
                self.mainViewController?.saveToDatabase()
                let finalVC = self.storyboard?.instantiateViewController(withIdentifier: "final_VC") as! FinalViewController
                finalVC.finalURL = "https://apps4sj.org/" + self.listId!
                finalVC.previewViewController = self
                self.present(finalVC, animated: true, completion: nil)
              }
          }
        } catch {
          print("JSON serialization failed: ", error)
        }
    }
    @IBAction func cancelButtonClicked() {
        dismiss(animated: true, completion: nil)
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        //let image = UIImage(data: jpegBuffer)
        var jpegData = Data()
        jpegData.append(contentsOf: Array(jpegBufferPointer!))
        let image = UIImage(data: jpegData)
        webpagePreview.image = image
        // Do any additional setup after loading the view.
    }
    
    func performPublishing() {
        var readStream: Unmanaged<CFReadStream>?
        var writeStream: Unmanaged<CFWriteStream>?
        CFStreamCreatePairWithSocketToHost(kCFAllocatorDefault,
                                             "apps4sj.org" as CFString,
                                             32421,
                                             &readStream,
                                             &writeStream)
        var inputStream: InputStream! = readStream!.takeRetainedValue()
        var outputStream: OutputStream! = writeStream!.takeRetainedValue()
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
}
