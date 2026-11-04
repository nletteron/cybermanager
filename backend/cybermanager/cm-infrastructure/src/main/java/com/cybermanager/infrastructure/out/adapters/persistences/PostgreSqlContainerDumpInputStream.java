package com.cybermanager.infrastructure.out.adapters.persistences;

import com.cybermanager.domain.exceptions.BusinessErrorCode;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;

class PostgreSqlContainerDumpInputStream extends FilterInputStream {

    private final Process process;
    private final Path error;
    private final PostgreSqlContainerCommands commands;
    private final BusinessErrorCode code;
    private boolean checked;

    PostgreSqlContainerDumpInputStream(Process process, Path error, PostgreSqlContainerCommands commands, BusinessErrorCode code) {
        super(process.getInputStream());
        this.process = process;
        this.error = error;
        this.commands = commands;
        this.code = code;
    }

    @Override
    public int read() throws IOException {
        int value = in.read();
        checkEnd(value);
        return value;
    }

    @Override
    public int read(byte[] bytes, int offset, int length) throws IOException {
        int count = in.read(bytes, offset, length);
        checkEnd(count);
        return count;
    }

    @Override
    public long transferTo(OutputStream output) throws IOException {
        long total = 0;
        byte[] buffer = new byte[8192];
        int count;
        while ((count = read(buffer, 0, buffer.length)) >= 0) {
            output.write(buffer, 0, count);
            total += count;
        }
        return total;
    }

    @Override
    public void close() throws IOException {
        try {
            super.close();
        } finally {
            if (process.isAlive()) {
                process.destroyForcibly();
            }
            commands.delete(error);
        }
    }

    private void checkEnd(int count) throws IOException {
        if (count < 0 && !checked) {
            checked = true;
            try {
                commands.check(process, error, code);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new IOException("Lecture du dump interrompue.", exception);
            }
        }
    }

}
