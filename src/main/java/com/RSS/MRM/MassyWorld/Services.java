package com.RSS.MRM.MassyWorld;

import com.RSS.MRM.MassyWorld.generated.PallierType;
import com.RSS.MRM.MassyWorld.generated.ProductType;
import com.RSS.MRM.MassyWorld.generated.World;

import javax.ws.rs.PUT;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

public class Services {

    public Services () {
    }

    public World readWorldFromXml(String username) {
        World world = new World ();
        try {
            JAXBContext cont = JAXBContext.newInstance(World.class);
            Unmarshaller u = cont.createUnmarshaller();
            String worldname=username+"-world.xml";
            InputStream input = null;
            try {
                input = new FileInputStream(worldname);
            }
            catch (Exception e) {
                input = getClass().getClassLoader().getResourceAsStream("world.xml");
            }

            world = (World) u.unmarshal(input);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return world;
    }

    public void saveWorldToXml (World world,String username) {
        try {
            JAXBContext cont = JAXBContext.newInstance(World.class);
            Marshaller m = cont.createMarshaller();
            m.marshal(world, new FileOutputStream(username+"-world.xml"));
        } catch (Exception e) {
            System.out.println("Erreur world.xml : "+e.getMessage());
            e.printStackTrace();
        }
    }

    public World getWorld(String username) {
        //updateWorld(username);
        return readWorldFromXml(username);
    }


    public Boolean updateWorld(String username) {
        World world = readWorldFromXml(username);
        List<ProductType> products = world.getProducts().getProduct();
        long tEcoule = System.currentTimeMillis() - world.getLastupdate();

        if(tEcoule >=0 ){
            for(ProductType p : products){
                if(p.isManagerUnlocked()==false) {
                    if(p.getTimeleft() <= tEcoule ){
                        world.setScore(world.getScore() + p.getRevenu());
                        world.setMoney(world.getMoney() + p.getRevenu());
                    } else{
                        p.setTimeleft(p.getTimeleft()-tEcoule);
                    }
                }else{
                    int qtProduit = (int) (tEcoule / p.getVitesse());
                    world.setMoney(world.getMoney()+(qtProduit*p.getRevenu()));
                    world.setScore(world.getScore()+(qtProduit*p.getRevenu()));
                    long tRestant = tEcoule % p.getVitesse();
                    if(tRestant > 0) p.setTimeleft(tRestant);
                }
            }
        }
        world.setLastupdate(System.currentTimeMillis());
        saveWorldToXml(world,username);
        return true;
    }

    private ProductType findProductById(World world, int idProduct) {
        ProductType product = new ProductType();
        List<ProductType> products = world.getProducts().getProduct();
        for(ProductType p : products){
            if(p.getId() == idProduct) return p;
            else product = null;
        }
        return product;
    }

    private PallierType findManagerByName(World world, String nameManager) {
        PallierType manager = new PallierType();
        List<PallierType> managers = world.getManagers().getPallier();
        for(PallierType m : managers) {
            if (m.getName().equals(nameManager)) manager = m;
            else manager = null;
        }
        return manager;
    }


    // renvoie false si l’action n’a pas pu être traitée
    public Boolean updateProduct(String username, ProductType newproduct) {
    // récupérer le monde du joueur
        World world = getWorld(username);
    // trouver le produit entré en paramètre
        ProductType product = findProductById(world, newproduct.getId());
        if (product == null) return false;

    // vérifier la variation de quantité
    //si qtchange > 0 : achat produit
    //si qtchange = 0 : lancement production
        int qtchange = newproduct.getQuantite() - product.getQuantite();
        if (qtchange > 0) {
            double capital = world.getMoney();
            double multiplicateur =0;
            double newcout = product.getCout();
            for(int i=0;i<qtchange;i++){
                multiplicateur = multiplicateur + (Math.pow(product.getCroissance(),i));
                newcout = newcout * product.getCroissance();
            }
            double cout = product.getCout()*multiplicateur;
            world.setMoney(capital-cout);
            product.setQuantite(newproduct.getQuantite());
            product.setCout(newcout);
        } else {
            world.setLastupdate(System.currentTimeMillis());
            product.setTimeleft(product.getVitesse());
        }
    // sauvegarder les changements
        saveWorldToXml(world,username);
        return true;
    }

    public Boolean updateManager(String username, PallierType newmanager) {
    // récupérer le monde du joueur
        World world = getWorld(username);
    // trouver le manager entré en paramètre
        PallierType manager = findManagerByName(world, newmanager.getName());
        if (manager == null) {return false;}

    // trouver le produit correspondant au manager
        ProductType product = findProductById(world, manager.getIdcible());
        if (product == null) {
            return false;
        }
        manager.setUnlocked(true);
        product.setManagerUnlocked(true);
        double capital = world.getMoney();
        double cout = manager.getSeuil();
        world.setMoney(capital-cout);

    // sauvegarder les changements au monde
        saveWorldToXml(world,username);
        return true;
    }
}
