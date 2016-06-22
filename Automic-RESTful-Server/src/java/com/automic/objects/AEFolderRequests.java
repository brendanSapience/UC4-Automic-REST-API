package com.automic.objects;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import com.uc4.api.objects.IFolder;
import com.uc4.communication.Connection;
import com.uc4.communication.requests.FolderTree;

public class AEFolderRequests {


	// Internal Method
	private static void addFoldersToList(ArrayList<IFolder> folderList,
			IFolder myFolder, boolean onlyExtractFolderObjects) {
		if(onlyExtractFolderObjects){
			if( myFolder.getType().equals("FOLD")){folderList.add(myFolder);}
			if( myFolder.getType().equals("FOLD") && myFolder.subfolder() != null){
				
				Iterator<IFolder> it0 = myFolder.subfolder();
				while (it0.hasNext()){
					addFoldersToList(folderList,it0.next(),onlyExtractFolderObjects);
				}
				}
		}else{
			folderList.add(myFolder);
			if(  myFolder.subfolder() != null){
				Iterator<IFolder> it0 = myFolder.subfolder();
				while (it0.hasNext()){
					addFoldersToList(folderList,it0.next(),onlyExtractFolderObjects);
				}
				}
		}

	}
	// Returns a list of ALL Folders (including folders in folders, folders in folders in folders etc.)
	public static ArrayList<IFolder> getFoldersRecursively(IFolder rootFolder, boolean OnlyExtractFolderObjects,Connection connection ) throws IOException{
		ArrayList<IFolder> FolderList = new ArrayList<IFolder>();
		if(!OnlyExtractFolderObjects){FolderList.add(getRootFolder(connection));}
		
		Iterator<IFolder> it = rootFolder.subfolder();
		while (it.hasNext()){
			IFolder myFolder = it.next();
			if(! myFolder.getName().equals("<No Folder>")){
				addFoldersToList(FolderList,myFolder,OnlyExtractFolderObjects);
			}
		}
		return FolderList; 
	}
	
	public static IFolder getRootFolder(Connection connection) throws IOException{
		FolderTree tree = new FolderTree();
		connection.sendRequestAndWait(tree);
		return tree.root();		
	}
	
	// Returns a list of ALL Folders (including folders in folders, folders in folders in folders etc.)
	public static ArrayList<IFolder> getAllFolders(boolean OnlyExtractFolderObjects, Connection connection) throws IOException{
		return getFoldersRecursively(getRootFolder(connection), OnlyExtractFolderObjects,connection);
	}
	
	// below method takes as an input either "AEV10 - 0005/UC4.APPLICATIONS/JFORUM_BREN"
	// or simply: "0005/UC4.APPLICATIONS/JFORUM_BREN"
	public static IFolder getFolderByFullPathName(String FolderName, Connection connection) throws IOException{
		 ArrayList<IFolder> allFolders = getAllFolders(true,connection);
		 for(IFolder folder : allFolders){
			 String FullPath = "";
			 // the IFolder.fullpath() method ALWAYS returns the system name before the path (ex: "AEV10 - 0005/UC4.APPLICATIONS/JFORUM_BREN")
			 // therefore it is necessary to modify it for comparison.. if the FolderName passed does not also contain the system name..
			 if(!FolderName.contains(" - ")){
				 FullPath = folder.fullPath().split(" - ")[1].trim();
			 }else{
				 FullPath = folder.fullPath().trim();
			 }
			 if(FullPath.equalsIgnoreCase(FolderName.trim())){
				 return folder;
			}
		 }

		 return null;
	}
	
}
