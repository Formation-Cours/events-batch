"use strict";
exports.__esModule = true;
var faker_1 = require("@faker-js/faker");
var node_fs_1 = require("node:fs");
var generateOneUser = function () {
    var user = {
        email: faker_1.fakerFR.internet.email(),
        firstName: faker_1.fakerFR.person.firstName(),
        lastName: faker_1.fakerFR.person.lastName(),
        password: faker_1.fakerFR.internet.password(),
        emailVerified: faker_1.fakerFR.datatype.boolean(),
        dateInscription: faker_1.fakerFR.date.between({ from: '2020-01-01T00:00:00.000Z', to: Date.now() }),
        avatar: faker_1.fakerFR.image.avatar(),
        address: {
            streetAddress: faker_1.fakerFR.location.streetAddress(),
            country: faker_1.fakerFR.location.country(),
            city: faker_1.fakerFR.location.city()
        }
    };
    return user;
};
var generateUsers = function (count) {
    var listUsers = [];
    for (var i = 0; i < count; i++) {
        listUsers.push(generateOneUser());
    }
    return listUsers;
};
var clean = function (list) {
    var cleanList = [];
    var seenEmails = new Set();
    for (var _i = 0, list_1 = list; _i < list_1.length; _i++) {
        var user = list_1[_i];
        if (!seenEmails.has(user.email)) {
            seenEmails.add(user.email);
            cleanList.push(user);
        }
    }
    return cleanList;
};
var writeFile = function (path, lineNumber) {
    console.log("Démarrage de la génération de la liste.");
    var listUsers = generateUsers(lineNumber);
    console.log("Nettoyage des doublons d'emails");
    var cleanList = clean(listUsers);
    var firsLine = "firstName,lastName,email,password,emailVerified,dateInscription,avatar,streetAddress,city,country;";
    console.log("Démarrage de la génération d'un tableau de string!");
    var userTabStr = cleanList.map(function (user) {
        return "".concat(user.firstName, ",").concat(user.lastName, ",").concat(user.email, ",").concat(user.password, ",").concat(user.emailVerified, ",").concat(user.dateInscription.toISOString(), ",").concat(user.avatar, ",").concat(user.address.streetAddress, ",").concat(user.address.city, ",").concat(user.address.country, ";");
    });
    var str = "".concat(firsLine, "\n").concat(userTabStr.join('\n'));
    try {
        console.log("Nombre d'utilisateur cr\u00E9\u00E9 dans ce fichier : ".concat(cleanList.length));
        console.log("Démarrage de l'écriture du fichier");
        (0, node_fs_1.writeFileSync)(path, str);
        console.log('Ecriture du fichier terminée!!');
    }
    catch (err) {
        console.error('err: ', err);
    }
};
writeFile('../users.csv', 1000);
// writeFile('../users.csv', 10);
// bun faker.ts
