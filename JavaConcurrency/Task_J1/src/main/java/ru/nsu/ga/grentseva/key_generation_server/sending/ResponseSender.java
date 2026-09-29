package ru.nsu.ga.grentseva.key_generation_server.sending;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import ru.nsu.ga.grentseva.key_generation_server.generation.GenerationResult;

public class ResponseSender implements Runnable {

    private final BlockingQueue<SendTask> queue = new LinkedBlockingQueue<>();
    private final Selector selector;

    public ResponseSender() throws IOException {
        selector = Selector.open();
    }

    public void submit(SendTask task) {
        queue.offer(task);
        selector.wakeup();
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                registerTasks();
                selector.select();
                sendResponses();
            }
        } catch (IOException exception) {
            System.err.println("Response sender stopped: " + exception.getMessage());
        } finally {
            closeSelector();
        }
    }

    private void registerTasks() throws IOException {
        SendTask task;

        while ((task = queue.poll()) != null) {
            SocketChannel channel = task.getConnection().getChannel();

            if (!channel.isOpen()) {
                continue;
            }

            SelectionKey key = channel.keyFor(selector);

            if (key == null || !key.isValid()) {
                key = channel.register(selector, SelectionKey.OP_WRITE,
                        new ArrayDeque<ByteBuffer>());
            }

            @SuppressWarnings("unchecked")
            Deque<ByteBuffer> buffers = (Deque<ByteBuffer>) key.attachment();

            buffers.add(prepareResponse(task.getResult()));
            key.interestOps(SelectionKey.OP_WRITE);
        }
    }

    private void sendResponses() {
        var iterator = selector.selectedKeys().iterator();

        while (iterator.hasNext()) {
            SelectionKey key = iterator.next();
            iterator.remove();

            if (!key.isValid() || !key.isWritable()) {
                continue;
            }

            SocketChannel channel = (SocketChannel) key.channel();

            @SuppressWarnings("unchecked")
            Deque<ByteBuffer> buffers = (Deque<ByteBuffer>) key.attachment();

            try {
                while (!buffers.isEmpty()) {
                    ByteBuffer buffer = buffers.peek();

                    int bytesWritten = channel.write(buffer);

                    if (bytesWritten == 0) {
                        break;
                    }

                    if (!buffer.hasRemaining()) {
                        buffers.poll();
                    }
                }

                if (buffers.isEmpty()) {
                    key.interestOps(0);
                }
            } catch (IOException exception) {
                key.cancel();

                try {
                    channel.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    private ByteBuffer prepareResponse(GenerationResult result) {
        byte[] privateKey = result.getPrivateKey().getEncoded();
        byte[] publicKey = result.getPublicKey().getEncoded();
        byte[] certificate = result.getCertificate();

        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES + privateKey.length
                        + Integer.BYTES + publicKey.length + Integer.BYTES + certificate.length);

        putData(buffer, privateKey);
        putData(buffer, publicKey);
        putData(buffer, certificate);

        buffer.flip();

        return buffer;
    }

    private void putData(ByteBuffer buffer, byte[] data) {
        buffer.putInt(data.length);
        buffer.put(data);
    }

    private void closeSelector() {
        try {
            selector.close();
        } catch (IOException exception) {
            System.err.println("Failed to close response selector: "
                    + exception.getMessage());
        }
    }
}