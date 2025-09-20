import SwiftUI

struct ConversationView: View {
    @ObservedObject var viewModel: ConversationViewModel
    @State private var messageText: String = ""

    var body: some View {
        VStack(spacing: 0) {
            if viewModel.isLoading && viewModel.messages.isEmpty {
                ProgressView("Loading messages…")
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else {
                ScrollViewReader { proxy in
                    List(viewModel.messages) { item in
                        MessageRow(message: item)
                            .listRowSeparator(.hidden)
                            .listRowBackground(Color.clear)
                            .id(item.id)
                    }
                    .listStyle(.plain)
                    .onChange(of: viewModel.messages) { messages in
                        guard let lastId = messages.last?.id else { return }
                        withAnimation(.easeOut) {
                            proxy.scrollTo(lastId, anchor: .bottom)
                        }
                    }
                }
            }

            if let error = viewModel.errorMessage {
                Text(error)
                    .font(.footnote)
                    .foregroundColor(.red)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(.horizontal)
                    .padding(.vertical, 8)
            }

            ComposerView(text: $messageText, isSending: viewModel.isSending) {
                let trimmed = messageText.trimmingCharacters(in: .whitespacesAndNewlines)
                guard trimmed.isEmpty == false else { return }
                viewModel.sendMessage(text: trimmed) {
                    messageText = ""
                }
            }
            .padding(.horizontal)
            .padding(.vertical, 12)
            .background(Color(uiColor: .secondarySystemBackground))
        }
        .navigationTitle("Conversation")
        .toolbar {
            ToolbarItem(placement: .navigationBarTrailing) {
                Button(action: viewModel.refresh) {
                    Image(systemName: "arrow.clockwise")
                }
                .disabled(viewModel.isLoading)
            }
        }
    }
}

struct MessageRow: View {
    let message: MessageItemViewModel

    var body: some View {
        HStack {
            if message.isOwn {
                Spacer(minLength: 32)
                bubble
            } else {
                bubble
                Spacer(minLength: 32)
            }
        }
        .padding(.vertical, 4)
        .padding(.horizontal)
    }

    private var bubble: some View {
        VStack(alignment: message.isOwn ? .trailing : .leading, spacing: 4) {
            Text(message.text)
                .foregroundColor(message.isOwn ? .white : .primary)
                .padding(10)
                .background(message.isOwn ? Color.accentColor : Color(uiColor: .systemGray5))
                .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
            Text(message.timestamp.formatted(date: .omitted, time: .shortened))
                .font(.caption2)
                .foregroundStyle(.secondary)
        }
    }
}

struct ComposerView: View {
    @Binding var text: String
    let isSending: Bool
    let onSend: () -> Void

    var body: some View {
        HStack(spacing: 12) {
            TextField("Message", text: $text, axis: .vertical)
                .lineLimit(1...4)
                .textFieldStyle(.roundedBorder)

            Button(action: onSend) {
                if isSending {
                    ProgressView()
                        .progressViewStyle(.circular)
                } else {
                    Image(systemName: "paperplane.fill")
                        .font(.system(size: 18, weight: .semibold))
                }
            }
            .disabled(isSending)
            .buttonStyle(.borderedProminent)
        }
    }
}
