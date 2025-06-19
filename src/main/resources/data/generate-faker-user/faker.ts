import { fakerFR as faker } from '@faker-js/faker';
import { writeFileSync } from 'node:fs';

interface User {
  email: string
  firstName: string
  lastName: string
  password: string
  emailVerified: boolean
  dateInscription: Date
  avatar: string
  address: Address
}

interface Address {
  streetAddress: string
  country: string
  city: string
}

const generateOneUser = () => {
  const user: User = {
    email: faker.internet.email(),
    firstName: faker.person.firstName(),
    lastName: faker.person.lastName(),
    password: faker.internet.password(),
    emailVerified: faker.datatype.boolean(),
    dateInscription: faker.date.between({ from: '2020-01-01T00:00:00.000Z', to: Date.now() }),
    avatar: faker.image.avatar(),
    address: {
      streetAddress: faker.location.streetAddress(),
      country: faker.location.country(),
      city: faker.location.city(),
    }
  };

  return user;
}


const generateUsers = (count: number): Array<User> => {
  const listUsers: Array<User> = [];

  for (let i = 0; i < count; i++) {
    listUsers.push(generateOneUser());
  }

  return listUsers;
}

const clean = (list: User[]): User[] => {
  const cleanList: User[] = [];
  const seenEmails = new Set();

  for (const user of list) {
    if (!seenEmails.has(user.email)) {
      seenEmails.add(user.email);
      cleanList.push(user);
    }
  }
  return cleanList;
}

const writeFile = (path: string, lineNumber: number) => {
  console.log("Démarrage de la génération de la liste.")
  let listUsers = generateUsers(lineNumber);

  console.log("Nettoyage des doublons d'emails")
  const cleanList = clean(listUsers);

  const firsLine = `firstName,lastName,email,password,emailVerified,dateInscription,avatar,streetAddress,city,country`

  console.log("Démarrage de la génération d'un tableau de string!")
  const userTabStr = cleanList.map((user) => {
    return `${user.firstName},${user.lastName},${user.email},${user.password},${user.emailVerified},${user.dateInscription.toISOString()},${user.avatar},${user.address.streetAddress},${user.address.city},${user.address.country};`
  })

  const str = `${firsLine}\n${userTabStr.join('\n')}`;
  try {
    console.log(`Nombre d'utilisateur créé dans ce fichier : ${cleanList.length}`)
    console.log("Démarrage de l'écriture du fichier")
    writeFileSync(path, str);
    console.log('Ecriture du fichier terminée!!');
  } catch (err) {
    console.error('err: ', err)
  }
}

writeFile('../users.csv', 1_000_000);
// writeFile('../users.csv', 10);

// bun faker.ts
