package fr.curie.navicell;

import java.util.UUID;
import java.nio.file.Path;
import org.springframework.data.annotation.Id;
import fr.curie.BiNoM.pathways.navicell.ProduceClickableMap;
import fr.curie.BiNoM.pathways.wrappers.*;
import fr.curie.navicell.storage.StorageException;
import fr.curie.navicell.storage.StorageService;

import org.sbml.x2001.ns.celldesigner.SbmlDocument;
import org.sbml.x2001.ns.celldesigner.CelldesignerPaintDocument;

public class NaviCellMap {

  @Id
  public String id;

  public String folder;
  public String name;
  public String networkPath;
  public String sbgnPath;
  public String imagePath;
  public String url;
  // public String configPath;
  
  private boolean createFolder(StorageService storage) {
    this.folder = UUID.randomUUID().toString();
    try {
      storage.createFolder(this.folder);  
      return true;
    }
    catch (StorageException e) {
      return false;
    }
  }
  public NaviCellMap() {
    
  }
  
  public NaviCellMap(StorageService storage) {
    
    boolean folder_created = this.createFolder(storage);
    while (!folder_created) {
      folder_created = this.createFolder(storage);
    }
  }

  // public NaviCellMap(String name, String network_path, String image_path, String config_path) {
  //   this.name = name;
  //   this.networkPath = network_path;
  //   this.imagePath = image_path;
  //   this.configPath = config_path;
    
  //   SbmlDocument cd = CellDesigner.loadCellDesigner(network_path);
  //   // cd.getSbml().getModel().
  // }

  @Override
  public String toString() {
    return String.format(
        "{'id': '%s', 'folder': '%s', 'name': '%s', 'network_path': '%s', 'sbgn_path': '%s', 'image_path': '%s'}",
        id, folder, name, networkPath, sbgnPath, imagePath);
  }

}