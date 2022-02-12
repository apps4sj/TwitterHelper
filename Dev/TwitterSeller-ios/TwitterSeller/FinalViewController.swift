//
//  FinalViewController.swift
//  TwitterSeller
//
//  Created by Apps4SI Club on 2/1/22.
//

import UIKit

class FinalViewController: UIViewController, UITextViewDelegate {
    var finalURL:String!
    var previewViewController:PreviewViewController!
    @IBOutlet var finalUrlTextView: UITextView!
    @IBOutlet var cancelButton: UIButton!
    
    @IBAction func cancelButtonClicked() {
        dismiss(animated: true, completion: nil)
        previewViewController.cancelButtonClicked()
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        let desc = "Your listing has been posted! View your list at "
        let attributedString = NSMutableAttributedString(string: desc + finalURL!)
        attributedString.addAttribute(.link, value: finalURL!, range:NSRange(location: desc.count, length: finalURL.count))
        finalUrlTextView.attributedText = attributedString
        //finalUrlTextView.delegate = self
    }
    
    func textView(_ textView: UITextView, shouldInteractWith URL: URL, in
                    characterRange: NSRange, interaction: UITextItemInteraction ) -> Bool {
        UIApplication.shared.open(URL)
        return false
    }
}
