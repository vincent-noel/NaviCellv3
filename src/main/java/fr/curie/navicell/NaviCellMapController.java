package fr.curie.navicell;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.boot.context.properties.EnableConfigurationProperties;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import fr.curie.navicell.storage.StorageProperties;
import fr.curie.navicell.storage.StorageService;


@CrossOrigin(origins = "http://localhost", maxAge = 3600)
@EnableConfigurationProperties(StorageProperties.class)
@RestController
public class NaviCellMapController {

  private final StorageService storageService;

  @Autowired
  private NaviCellMapRepository repository;
  
	@Autowired
	public NaviCellMapController(StorageService storageService) {
		this.storageService = storageService;
	}
  
  @GetMapping("/api/maps")
  @ResponseStatus(value = HttpStatus.OK)
  List<NaviCellMap> all() {
    return repository.findAll();
  }

  @DeleteMapping("/api/maps")
  @ResponseStatus(value = HttpStatus.OK)
  void deleteAll() {
    repository.deleteAll();
  }
  
  @PostMapping("/api/maps")
	@ResponseStatus(value = HttpStatus.CREATED)
	public void handleFileUpload(@RequestParam("name") String name, @RequestParam("network-file") MultipartFile network_file) {
    
    NaviCellMap entry = new NaviCellMap(this.storageService, name, network_file);
    repository.save(entry);
	}
}