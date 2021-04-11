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
        updateWorld(username);
        return readWorldFromXml(username);
    }


    public Boolean updateWorld(String username) {
        World world = readWorldFromXml(username);
        List<ProductType> products = world.getProducts().getProduct();
        long tEcoule = System.currentTimeMillis() - world.getLastupdate();
        if(tEcoule >=0 ){
            for(ProductType p : products){
                if(p.isManagerUnlocked()==false) {
                    if(p.getTimeleft() !=0) {
                        if (p.getTimeleft() <= tEcoule) {
                            world.setScore(Math.round((world.getScore() + (p.getRevenu()* (1+(world.getActiveangels()*world.getAngelbonus()/100))))*100 ));
                            world.setMoney( Math.round((world.getMoney() + (p.getRevenu()* (1+(world.getActiveangels()*world.getAngelbonus()/100))))*100 ));
                            p.setTimeleft(0);
                        } else {
                            p.setTimeleft(p.getTimeleft() - tEcoule);
                        }
                    }
                }else{
                    int qtProduit = (int) (tEcoule / p.getVitesse());
                    world.setMoney(Math.round(world.getMoney()+(qtProduit*p.getRevenu()* (1+(world.getActiveangels()*world.getAngelbonus()/100)))*100 ));
                    world.setScore(Math.round((world.getScore()+(qtProduit*p.getRevenu()* (1+(world.getActiveangels()*world.getAngelbonus()/100))))*100) );
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
        manager = null;
        for(PallierType m : managers) {
            if (m.getName().equals(nameManager)) manager = m;
        }
        return manager;
    }

    private PallierType findUpgradeByName(World world, String nameUpgrade) {
        PallierType upgrade = new PallierType();
        List<PallierType> upgrades = world.getUpgrades().getPallier();
        upgrade = null;
        for(PallierType m : upgrades) {
            if (m.getName().equals(nameUpgrade)) upgrade = m;
        }
        return upgrade;
    }

    private PallierType findAngelUpgradeByName(World world, String nameUpgrade) {
        PallierType upgrade = new PallierType();
        List<PallierType> upgrades = world.getAngelupgrades().getPallier();
        upgrade = null;
        for(PallierType m : upgrades) {
            if (m.getName().equals(nameUpgrade)) upgrade = m;
        }
        return upgrade;
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
            world.setMoney(Math.round((capital-cout)*100));
            product.setQuantite(newproduct.getQuantite());
            product.setCout(Math.round(newcout*100));
            List<PallierType> allunlocks = world.getAllunlocks().getPallier();
            List<PallierType> unlocks = product.getPalliers().getPallier();
            for (PallierType unlock : unlocks) {
                if( unlock.getSeuil() <= product.getQuantite() && unlock.isUnlocked() == false){
                    unlock.setUnlocked(true);
                    updateUnlock(world,product,unlock);
                }
            }
            for (PallierType allunlock : allunlocks) {
                if( allunlock.getSeuil() <= product.getQuantite() && allunlock.isUnlocked()==false){
                    allunlock.setUnlocked(true);
                    updateUnlock(world,product,allunlock);
                }
            }
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
        world.setMoney(Math.round((capital-cout)*100));

    // sauvegarder les changements au monde
        saveWorldToXml(world,username);
        return true;
    }

    public void updateUnlock(World world,ProductType product, PallierType newunlock){
        List<ProductType> products = world.getProducts().getProduct();
        switch(newunlock.getTyperatio()){
            case ANGE:
                world.setActiveangels(world.getActiveangels()+ (int) newunlock.getRatio());
                world.setAngelbonus((int)world.getActiveangels()*2);
                break;
            case GAIN:
                if(newunlock.getIdcible() == 0){
                    for (ProductType p: products) p.setRevenu(p.getRevenu()*newunlock.getRatio());
                }else {
                    product.setRevenu(product.getRevenu()*newunlock.getRatio());
                }
                break;
            case VITESSE:
                if(newunlock.getIdcible() == 0){
                    for (ProductType p: products) p.setVitesse((int) (p.getVitesse()/newunlock.getRatio()));
                }else {
                    product.setVitesse((int) (product.getVitesse()/newunlock.getRatio()));
                }
                break;
        }
    }


    public Boolean updateUpgrade(String username, PallierType newupgrade) {
        boolean isangelupgrade = false;
        // récupérer le monde du joueur
        World world = getWorld(username);
        // trouver l'upgrade entré en paramètre
        PallierType upgrade = findUpgradeByName(world, newupgrade.getName());
        PallierType angelupgrade = findAngelUpgradeByName (world, newupgrade.getName());
        if (upgrade == null) {
            if(angelupgrade == null) {return null;}
            else{upgrade = angelupgrade; isangelupgrade=true;}
        }

        List<ProductType> products = world.getProducts().getProduct();
        switch(upgrade.getTyperatio()){
            case ANGE:
                world.setActiveangels(world.getActiveangels()+(int) upgrade.getRatio());
                world.setAngelbonus((int)world.getActiveangels()*2);
                break;
            case GAIN:
                if(upgrade.getIdcible() == 0){
                    for (ProductType p: products) p.setRevenu(p.getRevenu()*upgrade.getRatio());
                }else {
                    // trouver le produit correspondant à l'upgrade
                    ProductType product = findProductById(world, upgrade.getIdcible());
                    if (product == null) {
                        return false;
                    }
                    product.setRevenu(product.getRevenu()*upgrade.getRatio());
                }
                break;
            case VITESSE:
                if(upgrade.getIdcible() == 0){
                    for (ProductType p: products) p.setVitesse((int) (p.getVitesse()/upgrade.getRatio()));
                }else {
                    // trouver le produit correspondant à l'upgrade
                    ProductType product = findProductById(world, upgrade.getIdcible());
                    if (product == null) {
                        return false;
                    }
                    product.setVitesse((int) (product.getVitesse()/upgrade.getRatio()));
                }
                break;
        }
        upgrade.setUnlocked(true);
        if(isangelupgrade == true){
            double activeA = world.getActiveangels();
            world.setActiveangels(activeA-upgrade.getSeuil());
            world.setAngelbonus(world.getAngelbonus()-(upgrade.getSeuil()*2));
        }else {
            double capital = world.getMoney();
            double cout = upgrade.getSeuil();
            world.setMoney(Math.round((capital - cout)*100));
        }

        // sauvegarder les changements au monde
        saveWorldToXml(world,username);
        return true;
    }

    public World delete(String username) throws JAXBException {

        JAXBContext cont = JAXBContext.newInstance(World.class);
        Unmarshaller u = cont.createUnmarshaller();
        InputStream input = getClass().getClassLoader().getResourceAsStream("world.xml");

        World world = getWorld(username);
        double score = world.getScore();
        double totalA = world.getTotalangels();
        double activeA = world.getActiveangels();
        int bonusA = world.getAngelbonus();

        double nbA = (150* Math.sqrt(score/Math.pow(10,15))) - totalA;
        if(nbA <0) nbA =0;

        World newWorld = (World) u.unmarshal(input);
        newWorld.setScore(Math.round(score*100));
        newWorld.setTotalangels((int)(totalA+nbA));
        newWorld.setActiveangels((int)(activeA+nbA));
        newWorld.setAngelbonus(bonusA);
        saveWorldToXml(newWorld,username);
        return newWorld;
    }
}

