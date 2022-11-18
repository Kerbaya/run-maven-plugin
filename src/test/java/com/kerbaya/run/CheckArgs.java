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

import java.util.Arrays;

public class CheckArgs
{
	public static void main(String[] args)
	{
		final int rc;
		if (Arrays.asList("zero", "one", "two").equals(Arrays.asList(args)))
		{
			System.out.println("args correct");
			rc = 0;
		}
		else
		{
			System.out.println("args incorrect: " + Arrays.asList(args));
			rc = 1;
		}
		System.exit(rc);
	}
}
