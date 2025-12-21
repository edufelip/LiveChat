import Foundation
import FirebaseStorage
import LiveChatCompose

final class FirebaseStorageBridge: NSObject, MediaStorageBridge {
    private let storage = Storage.storage()

    func uploadBytes(
        objectPath: String,
        bytes: KotlinByteArray,
        completionHandler: @escaping (String?, Error?) -> Void
    ) {
        let data = bytes.toData()
        let ref = storage.reference().child(objectPath)
        ref.putData(data, metadata: nil) { metadata, error in
            if let error = error {
                completionHandler(nil, error)
                return
            }
            ref.downloadURL { url, urlError in
                if let urlError = urlError {
                    completionHandler(nil, urlError)
                } else {
                    completionHandler(url?.absoluteString, nil)
                }
            }
        }
    }

    func downloadBytes(
        remoteUrl: String,
        maxBytes: Int64,
        completionHandler: @escaping (KotlinByteArray?, Error?) -> Void
    ) {
        let ref = storage.reference(forURL: remoteUrl)
        ref.getData(maxSize: maxBytes) { data, error in
            if let error = error {
                completionHandler(nil, error)
                return
            }
            guard let data = data else {
                completionHandler(nil, nil)
                return
            }
            completionHandler(KotlinByteArray.fromData(data), nil)
        }
    }

    func deleteRemote(remoteUrl: String, completionHandler: @escaping @Sendable (Error?) -> Void) {
        let ref = storage.reference(forURL: remoteUrl)
        ref.delete { error in
            completionHandler(error)
        }
    }

}

private extension KotlinByteArray {
    func toData() -> Data {
        var bytes = [UInt8](repeating: 0, count: Int(size))
        for index in 0..<Int(size) {
            bytes[index] = UInt8(bitPattern: get(index: Int32(index)))
        }
        return Data(bytes)
    }

    static func fromData(_ data: Data) -> KotlinByteArray {
        let byteArray = KotlinByteArray(size: Int32(data.count))
        data.withUnsafeBytes { buffer in
            for index in 0..<data.count {
                let value = buffer[index]
                byteArray.set(index: Int32(index), value: Int8(bitPattern: value))
            }
        }
        return byteArray
    }
}
