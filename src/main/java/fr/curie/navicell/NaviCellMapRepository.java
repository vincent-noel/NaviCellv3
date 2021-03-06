package fr.curie.navicell;

import java.util.List;
import java.nio.file.Path;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface NaviCellMapRepository extends MongoRepository<NaviCellMap, String> {

  public List<NaviCellMap> findByName(String name);
  public List<NaviCellMap> findByNetworkPath(String network_path);
  public List<NaviCellMap> findByImagePath(String image_path);
  
}