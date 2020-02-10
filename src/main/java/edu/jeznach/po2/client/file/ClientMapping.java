package edu.jeznach.po2.client.file;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;

/**
 * Represents mapping of a client file structure.
 * <p>Proper structure is:
 * <blockquote><pre><code>
 * 💻 <b>client</b> - location of client structure
 * ├ 📁 files - directory containing files that client stores on server
 * │ └ ⸬
 * ├ 📁 cancelled - directory containing files that client couldn't store on server
 * │ └ ⸬              due to storage limit
 * ├ 📁 shared - directory containing files that were shared to client
 * │ ├ 📁 user - directory containing files that /user/ shared to client
 * │ │ └ ⸬
 * │ └ ⸬
 * ├ 📄 mapping - file that contains mapping of this structure
 * └ 📄 log - file that contains messages logged by client
 * </code></pre></blockquote>
 */
public class ClientMapping {

    private @NotNull String name = "";
    /**
     * @return the username of client
     */
    public @NotNull String getName() { return this.name; }
    protected void setName(@NotNull String name) { this.name = name; }

    private @NotNull String log_name = "";
    /**
     * @return the name of file that is used for logging
     */
    public @NotNull String getLog_name() { return this.log_name; }
    protected void setLog_name(@NotNull String log_name) { this.log_name = log_name; }

    private @NotNull String client_location = "";
    /**
     * @return the absolute path to client structure
     */
    public @NotNull String getClient_location() { return this.client_location; }
    protected void setClient_location(@NotNull String client_location) { this.client_location = client_location; }

    private @NotNull Long used_space_bytes = 0L;
    /**
     * @return the current storage space occupied by client files, in bytes
     */
    public @NotNull Long getUsed_space_bytes() { return this.used_space_bytes; }
    protected void setUsed_space_bytes(@NotNull Long used_space_bytes) { this.used_space_bytes = used_space_bytes; }

    private @Nullable List<ClientFileMapping> files;
    /**
     * @return the list of mappings of files, that client stores on server
     */
    public @Nullable List<ClientFileMapping> getFiles() { return this.files; }
    protected void setFiles(@Nullable List<ClientFileMapping> files) { this.files = files; }

    private @Nullable List<FileMapping> cancelled_files;
    /**
     * @return the list of mappings of files, that server rejected due to storage limit
     */
    public @Nullable List<FileMapping> getCancelled_files() { return this.cancelled_files; }
    protected void setCancelled_files(@Nullable List<FileMapping> cancelled_files) { this.cancelled_files = cancelled_files; }

    private @Nullable List<SharedFileMapping> shared_files;
    /**
     * @return the list of mappings of files, that were shared to client
     */
    public @Nullable List<SharedFileMapping> getShared_files() { return this.shared_files; }
    protected void setShared_files(@Nullable List<SharedFileMapping> shared_files) { this.shared_files = shared_files; }

    protected ClientMapping() {  }

    protected ClientMapping(@NotNull InitParams params) {
        this.setName(params.clientName);
        this.setLog_name(params.logName);
        this.setClient_location(params.clientLocation.getAbsolutePath());
    }

    /**
     * Represents parameters injected to {@link ClientMapping} constructor
     */
    public static class InitParams {

        /**
         * Username of client.
         */
        protected @NotNull final String clientName;
        /**
         * Name that will be used for log file.
         */
        protected @NotNull final String logName;
        /**
         * Localisation of mapped client. This localisation should not contain {@link Directories}
         * nodes, that are not directories.
         */
        protected @NotNull final File clientLocation;

        /**
         * @param clientName the username of client
         * @param logName the name that will be used for log file
         * @param clientLocation the name that will be used for log file
         */
        public InitParams(@NotNull String clientName,
                          @NotNull String logName,
                          @NotNull File clientLocation) {
            this.clientName = clientName;
            this.logName = logName;
            this.clientLocation = clientLocation;
        }
    }

    /**
     * Represents possible names for directory nodes in client structure.
     */
    public enum Directories {
        /** Name of directory to hold user owned files */ files,
        /** Name of directory to hold user files that got cancelled from upload */ cancel,
        /** Name of directory to hold files shared to user */ shared
    }
}
