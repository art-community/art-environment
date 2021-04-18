package service

import constants.PROJECT_NAMES

fun projectName(alias: String) = PROJECT_NAMES[alias]!!
