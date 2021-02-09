package com.RSS.MRM.MassyWorld;

import com.RSS.MRM.MassyWorld.generated.World;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.FileOutputStream;
import java.io.InputStream;

public class Services {

    public Services () {
    }

    public World readWorldFromXml() {
        World world = new World ();
        try {
            JAXBContext cont = JAXBContext.newInstance(World.class);
            Unmarshaller u = cont.createUnmarshaller();
            InputStream input = getClass().getClassLoader().getResourceAsStream("world.xml");
            world = (World) u.unmarshal(input);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return world;
    }

    public void saveWorldToXml (World world) {
        try {
            JAXBContext cont = JAXBContext.newInstance(World.class);
            Marshaller m = cont.createMarshaller();
            m.marshal(world, new FileOutputStream("world.xml"));
        } catch (Exception e) {
            System.out.println("erreur marshall");
            e.printStackTrace();
        }
    }

    public World getWorld() {
        return readWorldFromXml();
    }

}
