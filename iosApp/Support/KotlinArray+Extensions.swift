import LiveChatShared

extension KotlinArray {
    func asArray<T>() -> [T] {
        var result: [T] = []
        for index in 0..<Int(size) {
            if let element = get(index: Int32(index)) as? T {
                result.append(element)
            }
        }
        return result
    }
}
