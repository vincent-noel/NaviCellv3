package fr.curie.navicell;
import java.util.List;
import java.util.Vector;


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

import fr.curie.BiNoM.pathways.utils.acsn.ACSNProcedures;
import fr.curie.BiNoM.pathways.navicell.ProduceClickableMap;
import org.apache.commons.io.FilenameUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;


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

  private static BufferedReader open_file(String filename)
	{
		try
		{
			return new BufferedReader(new FileReader(filename));
		}
		catch (FileNotFoundException e1)
		{
			System.err.println(e1.getMessage());
			System.exit(1);
			return null;
		}
	}
  private static String[][] load_xrefs(BufferedReader xref_stream, String xref_file)
	{
		try
		{
			Vector<String[]> ret = new Vector<String[]>();
			String line;
			while ((line = xref_stream.readLine()) != null) {
				// EV: 2017-05-26
				//String[] cols = line.replaceAll("#.*", "").split("\t");
				String[] cols = line.replaceAll("(^#.*| #.*)", "").split("\t");
				if (cols.length >= 3) {
					ret.add(cols);
				}
			}
			Object[] arr = ret.toArray();
			String[][] xrefs = new String[arr.length][];
			for (int nn = 0; nn < arr.length; ++nn) {
				xrefs[nn] = (String[])arr[nn];
			}
			return xrefs;

		}
		catch (IOException e1)
		{
			System.err.println("failed to load Xref file " + xref_file + ": " + e1.getMessage());
			System.exit(1);
			return null;
		}
  }
  
  @PostMapping("/api/maps")
	@ResponseStatus(value = HttpStatus.CREATED)
	public void handleFileUpload(@RequestParam("name") String name,
    @RequestParam("network-file") MultipartFile network_file, 
    @RequestParam("image-file") MultipartFile image_file
  ) {
    
    NaviCellMap entry = new NaviCellMap(this.storageService);
    
    String initials = "";
    for (String s : name.split(" ")) {
      initials+=s.charAt(0);
    }
    
    Path network_path = storageService.store(network_file, entry.folder, initials + "_master.xml");
    Path image_path = storageService.store(image_file, entry.folder, null);

    entry.name = name;
    entry.networkPath = network_path.toString();
    entry.imagePath = image_path.toString();
    
    try {
      String path = FilenameUtils.getPath(entry.imagePath);
      String ext = FilenameUtils.getExtension(entry.imagePath);
      String prefix = FilenameUtils.getPrefix(entry.imagePath);
      String fullprefix = prefix + path + initials + "_master-";
      
      BufferedImage map1 = ImageIO.read(new File(entry.imagePath));
		  int width = map1.getWidth();
      int max_zoom = 0;
      
      // How many division by two to be of "screen size"
      while ( width > 720) {
          width /= 2;
          max_zoom += 1;
      }
      
      // Copying with the proper name
      Path new_max_zoom = Files.copy(
        Paths.get(entry.imagePath),
        Paths.get(fullprefix + max_zoom + "." + ext),
        StandardCopyOption.REPLACE_EXISTING
      );
      
      // Making reduced resolution
      width = map1.getWidth();
      int zoom = max_zoom;
      String old_image_path, new_image_path;
      while ( width > 720 ) {
        
        if (zoom == max_zoom) {
          old_image_path = new_max_zoom.toString();
        } else {
          old_image_path = fullprefix + zoom + "." + ext;
        }
        new_image_path = fullprefix + (zoom-1) + "." + ext;
        
        ACSNProcedures.doScalePng(old_image_path, new_image_path);  
        
        width /= 2;
        zoom -= 1;
      }
      
      // Deleting original image (still here as max image)      
      Files.delete(Paths.get(entry.imagePath));
      
      final String[][] xrefs;
      
      BufferedReader xref_stream = open_file("/var/navicell/xrefs.txt");
      xrefs = load_xrefs(xref_stream, "/var/navicell/xrefs.txt");

      Files.createDirectories(Paths.get("/var/navicell/navicell/maps/" + name.replace(" ", "").toLowerCase()));
      
      ProduceClickableMap.run(
        initials + "_", new File(prefix+path), true, false, entry.name.replace(" ", ""), null, xrefs, true, 
        null, null, null, null, false, false, // Wordpress
        new File("/var/navicell/navicell/maps/" + entry.name.replace(" ", "").toLowerCase()),
        false, true, false
      );
      
      entry.url = "maps/" + entry.name.replace(" ", "").toLowerCase() + "/master/index.html";
      repository.save(entry);
    }
    catch (IOException e) {
      System.out.println(e);
    }
    catch (Exception e) {
      System.out.println(e);
    }
    
	}
}