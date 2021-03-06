/*
 * Copyright (C) 2004, 2005 Joe Walnes.
 * Copyright (C) 2006, 2007, 2008, 2009, 2011, 2012, 2013, 2014, 2015, 2017, 2018 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 *
 * Created on 14. August 2004 by Joe Walnes
 */
package com.thoughtworks.acceptance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.thoughtworks.acceptance.objects.StandardObject;
import com.thoughtworks.xstream.InitializationException;


public class ImplicitCollectionTest extends AbstractAcceptanceTest {

    public static class Farm extends StandardObject {
        private static final long serialVersionUID = 200408L;
        int size;
        List<Animal> animals = new ArrayList<>();

        public Farm(final int size) {
            this.size = size;
        }

        public void add(final Animal animal) {
            animals.add(animal);
        }
    }

    public static class Animal extends StandardObject {
        private static final long serialVersionUID = 200408L;
        String name;

        public Animal(final String name) {
            this.name = name;
        }
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        xstream.alias("zoo", Zoo.class);
        xstream.alias("farm", Farm.class);
        xstream.alias("animal", Animal.class);
        xstream.alias("dog", Dog.class);
        xstream.alias("cat", Cat.class);
        xstream.alias("room", Room.class);
        xstream.alias("house", House.class);
        xstream.alias("person", Person.class);
        xstream.alias("area", Area.class);
        xstream.alias("country", Country.class);
        xstream.ignoreUnknownElements();
    }

    public void testWithout() {
        final Farm farm = new Farm(100);
        farm.add(new Animal("Cow"));
        farm.add(new Animal("Sheep"));

        final String expected = ""
            + "<farm>\n"
            + "  <size>100</size>\n"
            + "  <animals>\n"
            + "    <animal>\n"
            + "      <name>Cow</name>\n"
            + "    </animal>\n"
            + "    <animal>\n"
            + "      <name>Sheep</name>\n"
            + "    </animal>\n"
            + "  </animals>\n"
            + "</farm>";

        assertBothWays(farm, expected);
    }

    public void testWithList() {
        final Farm farm = new Farm(100);
        farm.add(new Animal("Cow"));
        farm.add(new Animal("Sheep"));

        final String expected = ""
            + "<farm>\n"
            + "  <size>100</size>\n"
            + "  <animal>\n"
            + "    <name>Cow</name>\n"
            + "  </animal>\n"
            + "  <animal>\n"
            + "    <name>Sheep</name>\n"
            + "  </animal>\n"
            + "</farm>";

        xstream.addImplicitCollection(Farm.class, "animals");
        assertBothWays(farm, expected);
    }

    public void testWithReferencedImplicitElement() {
        final List<Object> list = new ArrayList<>();
        final Animal cow = new Animal("Cow");
        final Animal sheep = new Animal("Sheep");
        final Farm farm = new Farm(100);
        farm.add(cow);
        farm.add(sheep);
        list.add(cow);
        list.add(farm);
        list.add(sheep);

        final String expected = ""
            + "<list>\n"
            + "  <animal>\n"
            + "    <name>Cow</name>\n"
            + "  </animal>\n"
            + "  <farm>\n"
            + "    <size>100</size>\n"
            + "    <animal reference=\"../../animal\"/>\n"
            + "    <animal>\n"
            + "      <name>Sheep</name>\n"
            + "    </animal>\n"
            + "  </farm>\n"
            + "  <animal reference=\"../farm/animal[2]\"/>\n"
            + "</list>";

        xstream.addImplicitCollection(Farm.class, "animals");
        assertBothWays(list, expected);
    }

    public static class MegaFarm extends Farm {
        private static final long serialVersionUID = 200809L;
        String separator = "---";
        List<String> names;

        public MegaFarm(final int size) {
            super(size);
        }
    }

    public void testInheritsImplicitCollectionFromSuperclass() {
        xstream.alias("MEGA-farm", MegaFarm.class);

        final Farm farm = new MegaFarm(100); // subclass
        farm.add(new Animal("Cow"));
        farm.add(new Animal("Sheep"));

        final String expected = ""
            + "<MEGA-farm>\n"
            + "  <size>100</size>\n"
            + "  <animal>\n"
            + "    <name>Cow</name>\n"
            + "  </animal>\n"
            + "  <animal>\n"
            + "    <name>Sheep</name>\n"
            + "  </animal>\n"
            + "  <separator>---</separator>\n"
            + "</MEGA-farm>";

        xstream.addImplicitCollection(Farm.class, "animals");
        assertBothWays(farm, expected);
    }

    public void testSupportsInheritedAndDirectDeclaredImplicitCollectionAtOnce() {
        xstream.alias("MEGA-farm", MegaFarm.class);

        final MegaFarm farm = new MegaFarm(100); // subclass
        farm.add(new Animal("Cow"));
        farm.add(new Animal("Sheep"));
        farm.names = new ArrayList<>();
        farm.names.add("McDonald");
        farm.names.add("Ponte Rosa");

        final String expected = ""
            + "<MEGA-farm>\n"
            + "  <size>100</size>\n"
            + "  <animal>\n"
            + "    <name>Cow</name>\n"
            + "  </animal>\n"
            + "  <animal>\n"
            + "    <name>Sheep</name>\n"
            + "  </animal>\n"
            + "  <separator>---</separator>\n"
            + "  <name>McDonald</name>\n"
            + "  <name>Ponte Rosa</name>\n"
            + "</MEGA-farm>";

        xstream.addImplicitCollection(Farm.class, "animals");
        xstream.addImplicitCollection(MegaFarm.class, "names", "name", String.class);
        assertBothWays(farm, expected);
    }

    public void testInheritedAndDirectDeclaredImplicitCollectionAtOnceIsNotDeclarationSequenceDependent() {
        xstream.alias("MEGA-farm", MegaFarm.class);

        final MegaFarm farm = new MegaFarm(100); // subclass
        farm.add(new Animal("Cow"));
        farm.add(new Animal("Sheep"));
        farm.names = new ArrayList<>();
        farm.names.add("McDonald");
        farm.names.add("Ponte Rosa");

        final String expected = ""
            + "<MEGA-farm>\n"
            + "  <size>100</size>\n"
            + "  <animal>\n"
            + "    <name>Cow</name>\n"
            + "  </animal>\n"
            + "  <animal>\n"
            + "    <name>Sheep</name>\n"
            + "  </animal>\n"
            + "  <separator>---</separator>\n"
            + "  <name>McDonald</name>\n"
            + "  <name>Ponte Rosa</name>\n"
            + "</MEGA-farm>";

        xstream.addImplicitCollection(MegaFarm.class, "names", "name", String.class);
        xstream.addImplicitCollection(Farm.class, "animals");
        assertBothWays(farm, expected);
    }

    public void testAllowsSubclassToOverrideImplicitCollectionInSuperclass() {
        xstream.alias("MEGA-farm", MegaFarm.class);

        final Farm farm = new MegaFarm(100); // subclass
        farm.add(new Animal("Cow"));
        farm.add(new Animal("Sheep"));

        final String expected = ""
            + "<MEGA-farm>\n"
            + "  <size>100</size>\n"
            + "  <animal>\n"
            + "    <name>Cow</name>\n"
            + "  </animal>\n"
            + "  <animal>\n"
            + "    <name>Sheep</name>\n"
            + "  </animal>\n"
            + "  <separator>---</separator>\n"
            + "</MEGA-farm>";

        xstream.addImplicitCollection(MegaFarm.class, "animals");
        assertBothWays(farm, expected);
    }

    public void testAllowDifferentImplicitCollectionDefinitionsInSubclass() {
        xstream.alias("MEGA-farm", MegaFarm.class);

        final Farm farm = new Farm(10);
        farm.add(new Animal("Cod"));
        farm.add(new Animal("Salmon"));
        final MegaFarm megaFarm = new MegaFarm(100); // subclass
        megaFarm.add(new Animal("Cow"));
        megaFarm.add(new Animal("Sheep"));
        megaFarm.names = new ArrayList<>();
        megaFarm.names.add("McDonald");
        megaFarm.names.add("Ponte Rosa");

        final List<Farm> list = new ArrayList<>();
        list.add(farm);
        list.add(megaFarm);
        final String expected = ""
            + "<list>\n"
            + "  <farm>\n"
            + "    <size>10</size>\n"
            + "    <fish>\n"
            + "      <name>Cod</name>\n"
            + "    </fish>\n"
            + "    <fish>\n"
            + "      <name>Salmon</name>\n"
            + "    </fish>\n"
            + "  </farm>\n"
            + "  <MEGA-farm>\n"
            + "    <size>100</size>\n"
            + "    <animal>\n"
            + "      <name>Cow</name>\n"
            + "    </animal>\n"
            + "    <animal>\n"
            + "      <name>Sheep</name>\n"
            + "    </animal>\n"
            + "    <separator>---</separator>\n"
            + "    <name>McDonald</name>\n"
            + "    <name>Ponte Rosa</name>\n"
            + "  </MEGA-farm>\n"
            + "</list>";

        xstream.addImplicitCollection(Farm.class, "animals", "fish", Animal.class);
        xstream.addImplicitCollection(MegaFarm.class, "animals");
        xstream.addImplicitCollection(MegaFarm.class, "names", "name", String.class);
        assertBothWays(list, expected);
    }

    public static class House extends StandardObject {
        private static final long serialVersionUID = 200408L;
        private List<Room> rooms = new ArrayList<>();
        @SuppressWarnings("unused")
        private final String separator = "---";
        private List<Person> people = new ArrayList<>();

        public void add(final Room room) {
            rooms.add(room);
        }

        public void add(final Person person) {
            people.add(person);
        }

        public List<Person> getPeople() {
            return Collections.unmodifiableList(people);
        }

        public List<Room> getRooms() {
            return Collections.unmodifiableList(rooms);
        }
    }

    public static class Room extends StandardObject {
        private static final long serialVersionUID = 200408L;
        final String name;

        public Room(final String name) {
            this.name = name;
        }
    }

    public static class Person extends StandardObject {
        private static final long serialVersionUID = 200408L;
        final String name;
        final LinkedList<String> emailAddresses = new LinkedList<>();

        public Person(final String name) {
            this.name = name;
        }

        public void addEmailAddress(final String email) {
            emailAddresses.add(email);
        }
    }

    public void testDefaultCollectionBasedOnType() {
        final House house = new House();
        house.add(new Room("kitchen"));
        house.add(new Room("bathroom"));
        final Person joe = new Person("joe");
        joe.addEmailAddress("joe@house.org");
        joe.addEmailAddress("joe.farmer@house.org");
        house.add(joe);
        final Person jaimie = new Person("jaimie");
        jaimie.addEmailAddress("jaimie@house.org");
        jaimie.addEmailAddress("jaimie.farmer@house.org");
        jaimie.addEmailAddress("jaimie.ann.farmer@house.org");
        house.add(jaimie);

        final String expected = ""
            + "<house>\n"
            + "  <room>\n"
            + "    <name>kitchen</name>\n"
            + "  </room>\n"
            + "  <room>\n"
            + "    <name>bathroom</name>\n"
            + "  </room>\n"
            + "  <separator>---</separator>\n"
            + "  <person>\n"
            + "    <name>joe</name>\n"
            + "    <email>joe@house.org</email>\n"
            + "    <email>joe.farmer@house.org</email>\n"
            + "  </person>\n"
            + "  <person>\n"
            + "    <name>jaimie</name>\n"
            + "    <email>jaimie@house.org</email>\n"
            + "    <email>jaimie.farmer@house.org</email>\n"
            + "    <email>jaimie.ann.farmer@house.org</email>\n"
            + "  </person>\n"
            + "</house>";

        xstream.addImplicitCollection(House.class, "rooms", Room.class);
        xstream.addImplicitCollection(House.class, "people", Person.class);
        xstream.addImplicitCollection(Person.class, "emailAddresses", "email", String.class);

        final House serializedHouse = assertBothWays(house, expected);
        assertEquals(house.getPeople(), serializedHouse.getPeople());
        assertEquals(house.getRooms(), serializedHouse.getRooms());
    }

    @SuppressWarnings("unchecked")
    public void testWithEMPTY_LIST() {
        final House house = new House();
        house.people = Collections.EMPTY_LIST;
        house.rooms = Collections.EMPTY_LIST;
        xstream.addImplicitCollection(House.class, "rooms", Room.class);
        xstream.addImplicitCollection(House.class, "people", Person.class);
        final String expected = "" //
            + "<house>\n"
            + "  <separator>---</separator>\n"
            + "</house>";
        assertEquals(expected, xstream.toXML(house));
    }

    public void testWithEmptyList() {
        final House house = new House();
        house.people = Collections.emptyList();
        house.rooms = Collections.emptyList();
        xstream.addImplicitCollection(House.class, "rooms", Room.class);
        xstream.addImplicitCollection(House.class, "people", Person.class);
        final String expected = "" //
            + "<house>\n"
            + "  <separator>---</separator>\n"
            + "</house>";
        assertEquals(expected, xstream.toXML(house));
    }

    public static class Zoo extends StandardObject {
        private static final long serialVersionUID = 200602L;
        private final Set<Animal> animals;

        public Zoo() {
            this(new HashSet<Animal>());
        }

        public Zoo(final Set<Animal> set) {
            animals = set;
        }

        public void add(final Animal animal) {
            animals.add(animal);
        }
    }

    public void testWithSet() {
        final Zoo zoo = new Zoo();
        zoo.add(new Animal("Lion"));
        zoo.add(new Animal("Ape"));

        final String expected = ""
            + "<zoo>\n"
            + "  <animal>\n"
            + "    <name>Lion</name>\n"
            + "  </animal>\n"
            + "  <animal>\n"
            + "    <name>Ape</name>\n"
            + "  </animal>\n"
            + "</zoo>";

        xstream.addImplicitCollection(Zoo.class, "animals");
        assertBothWaysNormalized(zoo, expected, "zoo", "animal", "name");
    }

    public void testWithDifferentDefaultImplementation() {
        final String xml = ""
            + "<zoo>\n"
            + "  <animal>\n"
            + "    <name>Lion</name>\n"
            + "  </animal>\n"
            + "  <animal>\n"
            + "    <name>Ape</name>\n"
            + "  </animal>\n"
            + "</zoo>";

        xstream.addImplicitCollection(Zoo.class, "animals");
        xstream.addDefaultImplementation(TreeSet.class, Set.class);
        final Zoo zoo = xstream.fromXML(xml);
        assertTrue("Collection was a " + zoo.animals.getClass().getName(), zoo.animals instanceof TreeSet);
    }

    public void testWithSortedSet() {
        final Zoo zoo = new Zoo(new TreeSet<Animal>());
        zoo.add(new Animal("Lion"));
        zoo.add(new Animal("Ape"));

        final String expected = ""
            + "<zoo>\n"
            + "  <animal>\n"
            + "    <name>Ape</name>\n"
            + "  </animal>\n"
            + "  <animal>\n"
            + "    <name>Lion</name>\n"
            + "  </animal>\n"
            + "</zoo>";

        xstream.addImplicitCollection(Zoo.class, "animals");
        xstream.addDefaultImplementation(TreeSet.class, Set.class);
        assertBothWays(zoo, expected);
    }

    public static class Aquarium extends StandardObject {
        private static final long serialVersionUID = 200604L;
        final String name;
        final List<String> fish = new ArrayList<>();

        public Aquarium(final String name) {
            this.name = name;
        }

        public void addFish(final String fish) {
            this.fish.add(fish);
        }
    }

    public void testWithExplicitItemNameMatchingTheNameOfTheFieldWithTheCollection() {
        final Aquarium aquarium = new Aquarium("hatchery");
        aquarium.addFish("salmon");
        aquarium.addFish("halibut");
        aquarium.addFish("snapper");

        final String expected = ""
            + "<aquarium>\n"
            + "  <name>hatchery</name>\n"
            + "  <fish>salmon</fish>\n"
            + "  <fish>halibut</fish>\n"
            + "  <fish>snapper</fish>\n"
            + "</aquarium>";

        xstream.alias("aquarium", Aquarium.class);
        xstream.addImplicitCollection(Aquarium.class, "fish", "fish", String.class);

        assertBothWays(aquarium, expected);
    }

    public void testWithImplicitNameMatchingTheNameOfTheFieldWithTheCollection() {
        final Aquarium aquarium = new Aquarium("hatchery");
        aquarium.addFish("salmon");
        aquarium.addFish("halibut");
        aquarium.addFish("snapper");

        final String expected = ""
            + "<aquarium>\n"
            + "  <name>hatchery</name>\n"
            + "  <fish>salmon</fish>\n"
            + "  <fish>halibut</fish>\n"
            + "  <fish>snapper</fish>\n"
            + "</aquarium>";

        xstream.alias("aquarium", Aquarium.class);
        xstream.alias("fish", String.class);
        xstream.addImplicitCollection(Aquarium.class, "fish");

        assertBothWays(aquarium, expected);
    }

    public void testWithAliasedItemNameMatchingTheAliasedNameOfTheFieldWithTheCollection() {
        final Aquarium aquarium = new Aquarium("hatchery");
        aquarium.addFish("salmon");
        aquarium.addFish("halibut");
        aquarium.addFish("snapper");

        final String expected = ""
            + "<aquarium>\n"
            + "  <name>hatchery</name>\n"
            + "  <animal>salmon</animal>\n"
            + "  <animal>halibut</animal>\n"
            + "  <animal>snapper</animal>\n"
            + "</aquarium>";

        xstream.alias("aquarium", Aquarium.class);
        xstream.aliasField("animal", Aquarium.class, "fish");
        xstream.addImplicitCollection(Aquarium.class, "fish", "animal", String.class);

        assertBothWays(aquarium, expected);
    }

    public void testCanBeDeclaredOnlyForMatchingType() {
        try {
            xstream.addImplicitCollection(Animal.class, "name");
            fail("Thrown " + InitializationException.class.getName() + " expected");
        } catch (final InitializationException e) {
            assertTrue(e.getMessage().contains("declares no collection"));
        }
    }

    public void testWithNullElement() {
        final Farm farm = new Farm(100);
        farm.add(null);
        farm.add(new Animal("Cow"));

        final String expected = ""
            + "<farm>\n"
            + "  <size>100</size>\n"
            + "  <null/>\n"
            + "  <animal>\n"
            + "    <name>Cow</name>\n"
            + "  </animal>\n"
            + "</farm>";

        xstream.addImplicitCollection(Farm.class, "animals");
        assertBothWays(farm, expected);
    }

    public void testWithAliasAndNullElement() {
        final Farm farm = new Farm(100);
        farm.add(null);
        farm.add(new Animal("Cow"));

        final String expected = ""
            + "<farm>\n"
            + "  <size>100</size>\n"
            + "  <null/>\n"
            + "  <beast>\n"
            + "    <name>Cow</name>\n"
            + "  </beast>\n"
            + "</farm>";

        xstream.addImplicitCollection(Farm.class, "animals", "beast", Animal.class);
        assertBothWays(farm, expected);
    }

    public static class Area extends Farm {
        private static final long serialVersionUID = 201509L;

        @SuppressWarnings("hiding")
        List<Animal> animals = new ArrayList<>();

        public Area(final int size) {
            super(size);
        }

    }

    public void testWithHiddenList() {
        final Area area = new Area(1000);
        area.add(new Animal("Cow"));
        area.add(new Animal("Sheep"));
        area.animals.add(new Animal("Falcon"));
        area.animals.add(new Animal("Sparrow"));

        final String expected = ""
            + "<area>\n"
            + "  <size>1000</size>\n"
            + "  <animal defined-in=\"farm\">\n"
            + "    <name>Cow</name>\n"
            + "  </animal>\n"
            + "  <animal defined-in=\"farm\">\n"
            + "    <name>Sheep</name>\n"
            + "  </animal>\n"
            + "  <animal>\n"
            + "    <name>Falcon</name>\n"
            + "  </animal>\n"
            + "  <animal>\n"
            + "    <name>Sparrow</name>\n"
            + "  </animal>\n"
            + "</area>";

        xstream.addImplicitCollection(Farm.class, "animals");
        xstream.addImplicitCollection(Area.class, "animals");
        assertBothWays(area, expected);
    }

    public void testWithHiddenListAndDifferentAlias() {
        final Area area = new Area(1000);
        area.add(new Animal("Cow"));
        area.add(new Animal("Sheep"));
        area.animals.add(new Animal("Falcon"));
        area.animals.add(new Animal("Sparrow"));

        final String expected = ""
            + "<area>\n"
            + "  <size>1000</size>\n"
            + "  <domesticated defined-in=\"farm\">\n"
            + "    <name>Cow</name>\n"
            + "  </domesticated>\n"
            + "  <domesticated defined-in=\"farm\">\n"
            + "    <name>Sheep</name>\n"
            + "  </domesticated>\n"
            + "  <wild>\n"
            + "    <name>Falcon</name>\n"
            + "  </wild>\n"
            + "  <wild>\n"
            + "    <name>Sparrow</name>\n"
            + "  </wild>\n"
            + "</area>";

        xstream.addImplicitCollection(Farm.class, "animals", "domesticated", Animal.class);
        xstream.addImplicitCollection(Area.class, "animals", "wild", Animal.class);
        assertBothWays(area, expected);
    }

    public void testDoesNotInheritFromHiddenListOfSuperclass() {
        final Area area = new Area(1000);
        area.add(new Animal("Cow"));
        area.add(new Animal("Sheep"));
        area.animals.add(new Animal("Falcon"));
        area.animals.add(new Animal("Sparrow"));

        final String expected = ""
            + "<area>\n"
            + "  <size>1000</size>\n"
            + "  <animal defined-in=\"farm\">\n"
            + "    <name>Cow</name>\n"
            + "  </animal>\n"
            + "  <animal defined-in=\"farm\">\n"
            + "    <name>Sheep</name>\n"
            + "  </animal>\n"
            + "  <animals>\n"
            + "    <animal>\n"
            + "      <name>Falcon</name>\n"
            + "    </animal>\n"
            + "    <animal>\n"
            + "      <name>Sparrow</name>\n"
            + "    </animal>\n"
            + "  </animals>\n"
            + "</area>";

        xstream.addImplicitCollection(Farm.class, "animals");
        assertBothWays(area, expected);
    }

    public void testDoesNotPropagateToHiddenListOfSuperclass() {
        final Area area = new Area(1000);
        area.add(new Animal("Cow"));
        area.add(new Animal("Sheep"));
        area.animals.add(new Animal("Falcon"));
        area.animals.add(new Animal("Sparrow"));

        final String expected = ""
            + "<area>\n"
            + "  <size>1000</size>\n"
            + "  <animals defined-in=\"farm\">\n"
            + "    <animal>\n"
            + "      <name>Cow</name>\n"
            + "    </animal>\n"
            + "    <animal>\n"
            + "      <name>Sheep</name>\n"
            + "    </animal>\n"
            + "  </animals>\n"
            + "  <animal>\n"
            + "    <name>Falcon</name>\n"
            + "  </animal>\n"
            + "  <animal>\n"
            + "    <name>Sparrow</name>\n"
            + "  </animal>\n"
            + "</area>";

        xstream.addImplicitCollection(Area.class, "animals");
        assertBothWays(area, expected);
    }

    public static class County extends Area {
        private static final long serialVersionUID = 201509L;

        public County() {
            super(10);
        }
    }

    public static class Country extends County {
        private static final long serialVersionUID = 201509L;
        @SuppressWarnings("hiding")
        List<Animal> animals = new ArrayList<>();
    }

    public void testWithDoubleHiddenList() {
        final Country country = new Country();
        country.add(new Animal("Cow"));
        country.add(new Animal("Sheep"));
        ((Area)country).animals.add(new Animal("Falcon"));
        ((Area)country).animals.add(new Animal("Sparrow"));
        country.animals.add(new Animal("Wale"));
        country.animals.add(new Animal("Dolphin"));

        final String expected = ""
            + "<country>\n"
            + "  <size>10</size>\n"
            + "  <animal defined-in=\"farm\">\n"
            + "    <name>Cow</name>\n"
            + "  </animal>\n"
            + "  <animal defined-in=\"farm\">\n"
            + "    <name>Sheep</name>\n"
            + "  </animal>\n"
            + "  <animal defined-in=\"area\">\n"
            + "    <name>Falcon</name>\n"
            + "  </animal>\n"
            + "  <animal defined-in=\"area\">\n"
            + "    <name>Sparrow</name>\n"
            + "  </animal>\n"
            + "  <animal>\n"
            + "    <name>Wale</name>\n"
            + "  </animal>\n"
            + "  <animal>\n"
            + "    <name>Dolphin</name>\n"
            + "  </animal>\n"
            + "</country>";

        xstream.addImplicitCollection(Farm.class, "animals");
        xstream.addImplicitCollection(Area.class, "animals");
        xstream.addImplicitCollection(Country.class, "animals");
        assertBothWays(country, expected);
    }

    public static class Dog extends Animal {
        private static final long serialVersionUID = 201703L;

        public Dog(final String name) {
            super(name);
        }
    }

    public static class Cat extends Animal {
        private static final long serialVersionUID = 201703L;

        public Cat(final String name) {
            super(name);
        }
    }

    public void testCollectsDifferentTypesWithFieldOfSameName() {
        final Farm farm = new Farm(100);
        farm.add(new Dog("Lessie"));
        farm.add(new Cat("Garfield"));
        farm.add(new Cat("Felix"));
        farm.add(new Dog("Cujo"));
        farm.add(new Cat("Bob"));

        final String expected = ""
            + "<farm>\n"
            + "  <size>100</size>\n"
            + "  <dog>\n"
            + "    <name>Lessie</name>\n"
            + "  </dog>\n"
            + "  <cat>\n"
            + "    <name>Garfield</name>\n"
            + "  </cat>\n"
            + "  <cat>\n"
            + "    <name>Felix</name>\n"
            + "  </cat>\n"
            + "  <dog>\n"
            + "    <name>Cujo</name>\n"
            + "  </dog>\n"
            + "  <cat>\n"
            + "    <name>Bob</name>\n"
            + "  </cat>\n"
            + "</farm>";

        xstream.addImplicitCollection(Farm.class, "animals");
        assertBothWays(farm, expected);
    }
}
