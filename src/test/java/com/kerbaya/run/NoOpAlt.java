/*
 * Copyright 2022 Kerbaya Software
 * 
 * This file is part of run-maven-plugin. 
 * 
 * run-maven-plugin is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * run-maven-plugin is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with run-maven-plugin.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.kerbaya.run;

public class NoOpAlt
{
	public static void main(String[] args)
	{
		System.out.println(NoOpAlt.class.getName());
		System.exit(0);
	}
}
