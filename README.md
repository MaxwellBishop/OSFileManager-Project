# OSFileManager-Project

This simulation represents how the OS manages the long-term storage and the logical-to-physical mapping of data. This repository has a User-Space 
Virtual File System (VFS) that includes:

Basic CLI commands: open, close, read, write, touch, rm, cd, etc.
tree: a command that prints out a tree starting from current working directory
Virtual Disk that manages records
FileSystem that uses Serializable from java.io to save the tree and user structure into a .ser file "filesystem.ser"


# Getting started:

- Have java installed on your system (version 24 or higher)
- checkout the repo `bash git@github.com:MaxwellBishop/OSFileManager-Project.git` or download and open VFS_Project.java on your system
- Run the main function in the java file
- Enter in a username (can be existing or new user)
    - If first time running:
      - After entering a username, it will create a new FileSystem object and will save that object into the .ser file
      - "filesystem.ser" will be saved on your local system and whatever direcotry VFS_Project.java is in

# Extra notes:
.ser file is for saving your work. If you download the .java file mentioned earlier it will make your own local file "filesystem.ser" to save your work
Saving the workspace was for fun and was a extra challenge (technically not required for project)
Source code and testing was done in VSCode
